#!/bin/bash

echo "try: http://localhost:1234/search?q=abraham+lincoln"
java -jar ../target/homer-0.3.jar search --index=pages-index --port=1234
echo "Goodbye!"
