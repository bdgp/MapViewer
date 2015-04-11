%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%             Self-Organizing Map (SOM) for the TF annotation
% The steps for obtaining an SOM layout for the TFs are as follows
% 1. load the data from the excel spreadsheet and make it into a
% binary matrix;
% 2. collapse the TFs with the same expression annotation;
% 3. run the SOM on the collapse TFs;
% 4. jitter the SOM for visualization.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Step 1: loading data from the spreadsheet
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if ~exist('loadData')
    % table0208.xlsx is the annotation excel file
    [gmat, gsym, gdom, omat] = expression_import_big('./table0208.xlsx','all',1:16,1);
    loadData = 1;
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Step 2: collapsing TFs with exactly the same expression annotation
% full2col: the correspondence from the full TF list to the
% collapse list
% collapseTF: the annotation matrix of the colllapse list
% collapseTFName: the representative TF name of the collapse list
% collapseCount: the number of TFs represented by each collapse TF
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

temp = zeros(size(gmat,1),1);
full2col = cell(size(gmat,1),1); 
collapseTF = [];
collapseTFName = [];
collapseCount = [];
for i = 1:size(gmat,1)
    if temp(i) == 0
        collapseTF = [collapseTF; gmat(i,:)];
        collapseTFName = [collapseTFName; gsym(i)];
        count = 1;
        full2col(i) = gsym(i);
        for j = (i+1):size(gmat,1)        
            distTemp = sum(abs(gmat(i,:) - gmat(j,:)));            
            if distTemp == 0
                full2col(j) = gsym(i);
                temp(j) = 1;
                count = count + 1;
            end
        end
        collapseCount = [collapseCount;count];
    end            
end
    
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Step 3. Obtaining the Self-Organizing Map of the TF annotation
% codebook: cluster centroids for each SOM cluster
% bmus: which cluster the collapse TFs belong to? 
% Please do not change the parameter values unless you want a
% different SOM from the publication.
% We are using a modified version of Dahua Lin's somtoolbox (2009).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
doSOM = 1;
if doSOM

addpath('./somtoolbox/')
addpath('./somtoolbox/pwmetric/')

% parameters:
data = collapseTF;
qualityCheck = 1;
temp = repmat(1,1,100);
radius = 5:-0.01:1;
radius = [radius,temp];
nT = length(radius);
nF = 0;
shape = 'toroid';           
snapShotInd = [100,200,300,400,500];
metric = 'corrdist';
neigh = 'gaussian';
mapSize = 20;
lattice = 'hexa';

sMap = som_make(data,'msize', [mapSize mapSize],'shape', shape,'training',[nT,nF],'metric',metric,'tracking',1,'snapShot',snapShotInd,'radius',radius,'neigh',neigh,'lattice',lattice); 
[bmus qerrs] = som_bmus(sMap, data);
codebook = sMap.codebook;

% The following quantities are quality measures of the map
qe = sMap.qeT; % quantitative error of the SOM
te = sMap.teT; % topographic error of the SOM
bmuSwitch = sMap.bmuSwitch; 
snapShotM = sMap.snapShotM;
snapShotBmus = sMap.snapShotBmus;

if qualityCheck
    figure;plot(sMap.bmuSwitch);
    vline([100,200,300,400,500])
    xlabel('iteration')
    ylabel('number of switching data points')
    figure;plot(sMap.qeT);hold on;
    plot(sMap.teT,'red');
    vline([100,200,300,400,500])
    xlabel('iteration')
    ylabel('error measure')
    figure;hist(bmus,1:(length(unique(bmus))+200));
    xlabel('cluster index')
    ylabel('number of TFs per cluster')   
end

end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Step 4: Obtaining the coordinates for each TF in the SOM
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
addpath('./utilities/');

N = mapSize; M = mapSize; n = length(collapseTFName);
cbSOM = codebook;
coordN = zeros(mapSize^2,2);
k = 1;
for i = 1:mapSize
    for j = 1:mapSize
        coordN(k,:) = [i,j];
        k = k + 1;
    end
end


coordN(2:2:size(coordN,1)) = coordN(2:2:size(coordN,1)) + 0.5;
% Jittering for the purpose of data visualization:
somLayout = coordGen(coordN,bmus,N,M,n,.5);
figure; scatter(somLayout(:,1),somLayout(:,2));
text(somLayout(:,1),somLayout(:,2),collapseTFName);
save('./somLayout.mat','somLayout','collapseTFName','collapseTF','collapseCount','full2col');
