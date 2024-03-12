# Vector Tile Service

This repository provides the mean and manual to convert an osm file into vector tiles. This repo is specifically tailored for the alpine maps project where the main focus lies in providing up to date vector tiles for mountain peak labels. 

## Steps that our program is doing

1. download the appropriate osm file (e.g. https://download.geofabrik.de/europe/austria-latest.osm.pbf)
2. extract only the features we want to display in our vector tiles
3. convert the resulting feature specific osm file into vector tiles using planetiler


## how to use

0. download the appropriate osm map using 1_download.bat

1. change the extractor java code to your liking
2. execute 2_build.sh (to create the jar)
3. execute 3_extract.sh (to extract your data into a .pmtiles)
4. execute 4_server.sh (to provide the tiles (uses your local computer as the server))
4. alternative - move .pmtiles to desired server where they are served

Note:
all.bat / all.sh can be used for quickly changing something in the extractor.java file and seeing the results. 
Before using all.bat make sure that you have downloaded the latest map using the download.bat

## server

### probably prefered for aws
https://github.com/protomaps/PMTiles

### prefered for basic server (and debug)

https://github.com/protomaps/go-pmtiles

- download pmtiles from latest release
- execute ./server.sh

example url:
http://localhost:8080/austria.peaks/9/274/177.mvt



