# MapViewer

This is pre-release software and currently works out of the box only with the BDGP insitu MySQL database “insitu” (download the current MySQL dump from http://insitu.fruitfly.org). In addition to the database dump, some additional tables with the map information are needed. The tables and instructions are in DataHandling. 

Currently the database is hard-coded in server/SOMServiceImpl.java. Replace the database name in SOMServiceImpl.java and replace the line info=new InsituDatabase(db) with info=new SOMInfoGeneric(db) for a generic implementation. This will be fixed in the release.

The software has been extensively tested with Google GWT 2.5 but should work with the current release.
