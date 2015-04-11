function [gmat, gsym, gdom, omat] = expression_import_big(xls, sheet, os, pad)
% [gmat, gsym, gdom, omat] = expression_import_big(xls, sheet, os, pad)
% Parses Excel spreadsheet with OS specific expressiond data
% xls = filename of Excel spreadsheet
% sheet = name of sheet that should be parsed (in xls)
% os = either scalar or vector specifiying the OS to be parsed
%       scalar: 
%           returns the os as logical matrix in gmat, the symbols as cell in gsym, 
%           the domains as cell array in gdom and the other os's in omat as numbered
%           cell array with a matrix for each OS.
%       vector:
%           returns all os in the vector as logical matrix in gmat. 
%           gsym and gdom are as before. omat is not defined. 
%           e.g. use 1:16 for all OS, [1:2,7,16] for nervous system(Visual,
%           CNS, SNS & PNS)
% pad = if set, pads unused parts of OS with zeros.
%       e.g. PNS has only data for stage 3,4,5 and will return a matrix
%       with 3 columns. pad returns a matrix with 5 columns for each stage
%       range.
%
% Examples:
%
% expression_import_big('/Users/erwin/Desktop/BDGP/TF paper/OSEXPMAT-annotated-2011-09-22-1.xlsx','OSEXPMAT-annotated-2011-09-22',16,1)
% returns PNS as 5x78 matrix
% expression_import_big('/Users/erwin/Desktop/BDGP/TF paper/OSEXPMAT-annotated-2011-09-22-1.xlsx','OSEXPMAT-annotated-2011-09-22',1:2,1)
% returns Visual System and PNS as matrix
% expression_import_big('/Users/erwin/Desktop/BDGP/TF paper/OSEXPMAT-annotated-2011-09-22-1.xlsx','OSEXPMAT-annotated-2011-09-22',1:16,1)
% returns everything as big matrix
%

OS_MAX = 16;


% Import the data
[~, ~, raw] = xlsread(xls,sheet);
cell_mat = raw(2:end,4:67);
%cell_mat = raw(:,4:72);
asym = raw(2:end,1);
adom = raw(2:end,3);

% Create output variable
all_gmat = cell2mat(cell_mat);

osrange = zeros(OS_MAX, 2);
oscont = cell(OS_MAX, 1);

for os_n = 1:OS_MAX
    range_os = find(max(all_gmat) == os_n);
    
    osmat = all_gmat(:,min(range_os):max(range_os));
    osmat = osmat == os_n;
    
    osexp = find(max(osmat,[],2) == 1);
    
    [gsym, osunique] = unique(asym(osexp));
    
    osrange(os_n, :) = [min(range_os),max(range_os)];
    oscont{os_n} = osexp(osunique);
end

maxrange = max(max(osrange(:,2) - osrange(:,1),[],2)) + 1;


if length(os) == 1,
    % extract single OS
    
    omat = cell(OS_MAX, 1);
    for os_n = 1:OS_MAX,
        omat{os_n} = all_gmat(oscont{os}, osrange(os_n, 1):osrange(os_n, 2));
        omat{os_n} = omat{os_n} == os_n;

        if exist('pad'),
            if maxrange - size(omat{os_n},2) > 0,
                omat{os_n} = [zeros(size(omat{os_n},1), maxrange - size(omat{os_n},2)), omat{os_n}];
            end
        end

    end


    gmat = omat{os};
    gsym = asym(oscont{os});
    gdom = adom(oscont{os});

else
    % extract multiple OS
    
    gsym={};
    gdom={};
    
    os_ct=0;
    st_ct=0;
    for os_n = os,
        st_ct = st_ct + osrange(os_n, 2) - osrange(os_n, 1);
        os_ct = os_ct + length(oscont{os_n});
    end    
    if exist('pad'),
        gmat = zeros(os_ct, length(os) * 5);
    else
        gmat = zeros(os_ct, st_ct);
    end
    
    omat = cell(length(os), 1);
    os_ct=1;
    for os_sel = os,
        st_ct = 1;
        for os_n = os,
            pmat = all_gmat(oscont{os_sel}, osrange(os_n, 1):osrange(os_n, 2));
            pmat = pmat == os_n;

            if exist('pad'),
                if maxrange - size(pmat,2) > 0,
                    pmat = [zeros(size(pmat,1), maxrange - size(pmat,2)), pmat];
                end
            end

            % omat{os_n} = pmat;
            gmat(os_ct:os_ct+size(pmat,1)-1,st_ct:st_ct+size(pmat,2)-1) = pmat;
            st_ct = st_ct+size(pmat,2);

        end
        
        os_ct = os_ct+size(pmat,1);
        gsym = {gsym{:}, asym{oscont{os_sel}}};
        gdom = {gdom{:}, adom{oscont{os_sel}}};
    end
    
    [gsym, guni] = unique(gsym);
    gdom=gdom(guni);
    gmat=gmat(guni,:);
    
    
end




% [gu, gi] = unique(gsym);
% 
% if length(gi) ~= length(gsym)
%     fprintf('Warning: symbols not unique - pruning them\n');
%     gsym = gu;
%     gmat = gmat(gi,:);
% end
