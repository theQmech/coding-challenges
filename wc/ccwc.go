package main

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"log"
	"os"
	"strings"
)

func main() {
	readerSource := os.Stdin
	lastArg := string(os.Args[len(os.Args)-1])
	var filename string
	if lastArg == "-" {
		filename = lastArg
	} else if !strings.HasPrefix(lastArg, "-") {
		file, err := os.Open(lastArg)
		if err != nil {
			log.Fatal(err)
		}
		defer file.Close()
		readerSource = file
		filename = lastArg
	} else {
		// ...
	}
	reader := bufio.NewReader(readerSource)

	numBytes := int64(0)
	numLines := 0
	numWords := 0
	var buf []byte
	var err error
	for err == nil {
		buf, err = reader.ReadBytes('\n')

		numBytes += int64(len(buf))
		numWords += len(strings.Fields(string(buf)))
		if err != io.EOF {
			numLines += 1
		}
	}

	println("==>", numBytes, numLines, filename)

	var printBytes = flag.Bool("c", false, "prints number of bytes")
	var printWords = flag.Bool("w", false, "prints number of words")
	var printLines = flag.Bool("l", false, "prints number of lines")

	flag.Parse() // This will parse all the arguments from the terminal

	if *printLines {
		fmt.Printf("\t%d", numLines)
	}
	if *printWords {
		fmt.Printf("\t%d", numWords)
	}
	if *printBytes {
		fmt.Printf("\t%d", numBytes)
	}
	fmt.Printf("\n")
}
