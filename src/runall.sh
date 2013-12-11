#!/bin/bash
FILES=/home/ugrads/coms2011/jh1288/linux/spamfilter/train/*
for f in $FILES
do
  echo "Processing $f file.."
  java Filter $f
  # take action on each file. $f store current file name
done