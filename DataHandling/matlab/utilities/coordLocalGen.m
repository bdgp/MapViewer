function coord = coordLocalGen(n,radius)
xCoord = zeros(n,1);
yCoord = zeros(n,1);
currentInd = 2;
r = 2;
numOnTheRing = 2^(r+1);
unitAngel = 2*pi/numOnTheRing;
i = 1;
while currentInd<=n
        xCoord(currentInd) = (r-1)*radius*cos(i*unitAngel);        
        yCoord(currentInd) = (r-1)*radius*sin(i*unitAngel);
        i = i + 1;
        currentInd = currentInd + 1;
        if i>numOnTheRing
          i = 1;
          r = r + 1;
          numOnTheRing = 2^(r+1);
          unitAngel = 2*pi/numOnTheRing;
        end
end
coord = [xCoord,yCoord];