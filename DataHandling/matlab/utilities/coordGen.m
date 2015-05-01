function coord = coordGen(pts,bmus,N,M,n,radius)
numCat = N*M;
coord = zeros(n,2);
for k = 1:numCat
    tfIndices = find(bmus == k);
    numTemp = length(tfIndices);
    if (numTemp > 0)
        localCoord = coordLocalGen(numTemp,radius);
        centTemp = pts(k,:);
        temp = repmat(centTemp,numTemp,1);
        coordTemp = temp + localCoord;
        coord(tfIndices,:) = coordTemp;
    end
end

end