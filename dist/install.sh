#!/bin/bash

# Endodontic Measurements 2.0 Installer for Mac/Linux

JAR_NAME="Endodontic_Measurements_2.0.jar"
CONFIG_NAME="Endodontic_Measurements.cfg"

# Common ImageJ plugin directories
POSSIBLE_DIRS=(
    "/Applications/ImageJ.app/plugins"
    "/Applications/ImageJ/plugins"
    "$HOME/Library/Application Support/ImageJ/plugins"
    "$HOME/ImageJ/plugins"
    "/usr/local/share/imagej/plugins"
    "../plugins" # If run from ImageJ root
)

DEST_DIR=""

echo "Installing Endodontic Measurements 2.0..."

# Try to find installation directory
for dir in "${POSSIBLE_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        DEST_DIR="$dir/Endodontic_Measurements_2.0"
        echo "Found ImageJ plugins directory at: $dir"
        break
    fi
done

# If not found, ask user
if [ -z "$DEST_DIR" ]; then
    echo "Could not automatically find ImageJ plugins directory."
    read -p "Please enter the path to your ImageJ/plugins folder: " USER_PATH
    if [ -d "$USER_PATH" ]; then
        DEST_DIR="$USER_PATH/Endodontic_Measurements_2.0"
    else
        echo "Error: Directory does not exist."
        exit 1
    fi
fi

# Create directory
mkdir -p "$DEST_DIR"
if [ $? -ne 0 ]; then
    echo "Error: Failed to create directory $DEST_DIR"
    echo "You may need to run with sudo."
    exit 1
fi

# Copy files
cp "$JAR_NAME" "$DEST_DIR/"
cp "$CONFIG_NAME" "$DEST_DIR/"

if [ $? -eq 0 ]; then
    echo "Installation successful!"
    echo "Files installed to: $DEST_DIR"
    echo "Please restart ImageJ."
else
    echo "Error: Failed to copy files."
    exit 1
fi
