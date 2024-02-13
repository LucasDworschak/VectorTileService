# Vector Tile Servic

This repository provides the mean and manual to convert an osm file into vector tiles. This repo is specifically tailored for the alpine maps project where the main focus lies in providing up to date vector tiles for mountain peak labels. 

## Steps that our program is doing

1. (TODO) download the appropriate osm file (e.g. https://download.geofabrik.de/europe/austria-latest.osm.pbf)
2. extract only the features we want to display in our vector tiles
3. convert the resulting feature specific osm file into vector tiles using planetiler


## how to use

1. change the extractor java code to your liking
2. execute build.sh (to create the jar)
3. execute extract.sh (to extract your data into a .pmtiles)
4. execute server.sh (to provide the tiles (uses your local computer as the server))
4. alternative - move .pmtiles to desired server where they are served



## server

### probably prefered for aws
https://github.com/protomaps/PMTiles

### prefered for basic server (and debug)

https://github.com/protomaps/go-pmtiles

- download pmtiles from latest release
- execute ./server.sh

example url:
http://localhost:8080/austria.peaks/9/274/177.mvt



