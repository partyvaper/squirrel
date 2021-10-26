#!/bin/bash
wget https://archive.runestats.com/latest.tar.gz
mkdir _latest
tar -xzvf latest.tar.gz -C _latest
mv _latest/cache cache
rm -rf _latest latest.tar*