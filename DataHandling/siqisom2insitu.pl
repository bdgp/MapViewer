#!/usr/bin/perl


use strict;
use Getopt::Long;
use DBI;

my $OVERLAY_START = 4;


my ($dbname, $host, $user, $s_title, $s_color, $link_insitu);

GetOptions(
        'dbname=s' => \$dbname,
        'host=s' => \$host,
        'user=s' => \$user,
	'title=s' => \$s_title,
	'colorfile=s' => \$s_color,
	'insitu=s' => \$link_insitu,
        );

if ( ! defined $dbname ) {
        print STDERR "$0 --dbname=<dbname> [--host=<host>] <filename or STDIN>\n";
	print STDERR "Optional:\n";
	print STDERR "\t--host DB host\n";
	print STDERR "\t--user DB user\n";
	print STDERR "\t--title Title of som, otherwise filename\n";
	print STDERR "\t--colorfile File with colorinfo\n";
	print STDERR "\t\tEntry Color\n";
        exit;
}


my $dbh = connectDB($host, $dbname, $user);

if ( ! defined $dbh ) {
	print STDERR "Cannot connect to database\n";
	exit;
}

my $stat;
# Inserts into somtables
$stat = "insert into somtitle(name) values(?)";
my $sth_ititle = $dbh->prepare($stat);
$stat = "insert into somstruct(id,name,x,y,somtitle_id) values(?,?,?,?,?)";
my $sth_istruct = $dbh->prepare($stat);
$stat = "insert into somoverlay_info(name,variant,color,type,decorator,somtitle_id) values(?,?,?,?,?,?)";
my $sth_iovinfo = $dbh->prepare($stat);
$stat = "insert into somoverlay(somstruct_id,somoverlay_info_id) values(?,?)";
my $sth_iov = $dbh->prepare($stat);

# Number/count queries
$stat = "select max(id) from somstruct";
my $sth_qmaxstruct = $dbh->prepare($stat);
$stat = "select count(somstruct_id) from somoverlay where somoverlay_info_id = ?";
my $sth_qctstruct = $dbh->prepare($stat);

# Id queries
$stat = "select id from somtitle where name = ?";
my $sth_qtitle = $dbh->prepare($stat);
$stat = "select id from somstruct where name = ?";
my $sth_qstruct = $dbh->prepare($stat);
$stat = "select id from somoverlay_info where name = ? and variant = ? and color = ? and type = ? and decorator = ? and somtitle_id = ?";
my $sth_qovinfo = $dbh->prepare($stat);

# Queries for linking to insitu db
$stat = "insert into som2main(somstruct_id, main_id) select somstruct.id, main.id  from somstruct, main, tmp_TF2010 where somstruct.name = tmp_TF2010.gene_id and tmp_TF2010.est_id = main.est_id and somtitle_id = ?";
my $sth_iinsitu =  $dbh->prepare($stat);
$stat = "insert into som2main (somstruct_id, main_id) select somstruct.id, main.id  from somstruct, main, tmp_TF2010 where substring(somstruct.name from 2) = tmp_TF2010.gene_id and tmp_TF2010.est_id = main.est_id and somstruct.name like \'+\%\' and somtitle_id = ?";
my $sth_iinsitu_pfudge =  $dbh->prepare($stat);


my $infn =  $ARGV[0];
if ( ! defined $infn || $infn eq "" ) {
	$infn="stdin";
	print STDERR "Warning: Setting filename to STDIN\n";
}


my %colors;
my %types;
my %decorators;
if ( defined $s_color ) {
	open(FC, $s_color) || die "Metadata file not found";
	while ( <FC> ) {
		chomp;
		next if ( $_ =~ /^#/ || $_ =~ /^\s/ );
		# my @le = split(" ");
		my @le = &splittab($_);
		$colors{$le[0]} = $le[1];
		$types{$le[0]} = $le[2];
		$decorators{$le[0]} = $le[3];
		print "Found metadata for $le[0]: $le[1], $le[2], $le[3]\n";
	}
	close FC;
}

#
# Begin database work


# Insert title - make sure it doesn't exist
my $title;
if ( defined $s_title ) {
	$title = $s_title;
} else {
	$title = $infn;
}
$sth_qtitle->execute($title);
my $title_id = &db_id($sth_qtitle, $title);
if ( defined $title_id ) {
	print STDERR "Warning: Title exists - reusing\n";
} else {
	$sth_ititle->execute($title);
	$title_id = &db_id($sth_qtitle, $title);
}

if (! defined $title_id or $title_id == 0 ) {
	print STDERR "ERROR: Something went wrong inserting title $title\n";
	exit;
}


# Parse first line
my $line = <>;
chomp $line;
if ( $line !~ /^TFName/ && $line !~ /^Label/ ) {
	print STDERR "ERROR: Unexpected first line - aborting\n";
	exit
}
my @header = split(" ", $line);

my @ovinfo;
for ( my $i = $OVERLAY_START; $i < scalar(@header); $i++ ) {
	$header[$i] =~ /(.+?)\_(\d+)/;
	my $h_name = $1;
	my $h_var = $2;
	if ( ! defined $h_name || ! defined $h_var ) {
		print STDERR "ERROR: Unexpected first line entry - aborting\n";
		exit;
	}
	my $col = "AAAAAA";
	my $typ = "Types";
	my $dec = "Venn";
	if (defined %colors) {
		if ( defined $colors{$h_name} ) {
			$col = $colors{$h_name};
		} else {
			printf STDERR "Warning: No color found for $h_name - using generic one\n";
		}
		if ( defined $types{$h_name} ) {
			$typ = $types{$h_name};
		} else {
			printf STDERR "Warning: No type found for $h_name - using generic one\n";
		}
		if ( defined $decorators{$h_name} ) {
			$dec = $decorators{$h_name};
		} else {
			printf STDERR "Warning: No decorator found for $h_name - using generic one\n";
		}
	}
	
	
	my $ovinfo_id = &db_id($sth_qovinfo, $h_name, $h_var, $col, $typ, $dec, $title_id);
	
	if ( ! defined $ovinfo_id ) {
		print "Info: Inserting $h_name, $h_var, $col\n";
		$sth_iovinfo->execute($h_name, $h_var, $col, $typ, $dec, $title_id);
		$ovinfo_id = &db_id($sth_qovinfo, $h_name, $h_var, $col, $typ, $dec, $title_id);
	} else {
		print "Info: Found existing info entry for $h_name, $h_var, $col\n";
	}
	
	if ( ! defined $ovinfo_id ) {
		print STDERR "ERROR: Cannot confirm $h_name, $h_var, $col entry\n";
		exit;
	}
	push @ovinfo, $ovinfo_id;
}

# Find smallest id
$sth_qmaxstruct->execute();
my ($s_id) = $sth_qmaxstruct->fetchrow_array();
$s_id += 1;


while ( <> ) {
        chomp;
        
        
        # parse line
        # TFName X1 X2 countTF VisualPr_1 VisualPr_2 ...

        my ($name, $x, $y, $ct, @ov) = split(" ");
        
        print "Info: Inserting $name ($s_id) ... ";        
        $sth_istruct->execute($s_id, $name, $x, $y, $title_id);
        
       	my $ctov = 0;
        for ( my $i = 0; $i < scalar(@ov); $i++ ) {
        	if ( @ov[$i] != 0 ) {
        		$sth_iov->execute($s_id, $ovinfo[$i]) || print STDERR "Warning: Failed to insert overlay $i for $name\n";
        		$ctov += 1;
        	}
        }
        
        $s_id += 1;
        
	print "found $ctov overlays, done\n";
}


sub splittab {
	my ($line) = @_;
	
	my @le = split /\t/, $line;
	my @lr = ();
	
	foreach my $e ( @le ) {
		if ( defined $e && $e ne "" ) {
			push @lr, $e;
		}
	}
	
	return @lr;
}

sub connectDB {
	my ($host, $dbname, $userid, $password, $port) = @_;

        my $connect = "dbi:mysql:dbname=$dbname";
        if ( defined $host ) {
                $connect .= ";host=$host";
        }
        if ( defined $port ) {
                $connect .= ";port=$port";
        }
        if ( ! defined $userid ) {
                $userid = "";
        }
        if ( ! defined $password ) {
                $password = "";
        }
	
	my $dbh = DBI->connect($connect, $userid, $password);
	
	return $dbh;
}

sub db_id {
	my ($sth, @query) = @_;

	$sth->execute(@query);
	my ($id) = $sth->fetchrow_array();

	return $id;
}

