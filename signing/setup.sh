#!/bin/sh

ENCRYPT_KEY=$1

if [ -n "$ENCRYPT_KEY" ]; then
  openssl aes-256-cbc -md sha256 -d -a -in signing/app-upload.aes -out signing/app-upload.jks -k "$ENCRYPT_KEY"
  openssl aes-256-cbc -md sha256 -d -a -in signing/play-account.aes -out signing/play-account.json -k "$ENCRYPT_KEY"
else
  echo "ENCRYPT_KEY is empty"
fi
