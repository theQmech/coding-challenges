package main

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"log"
	"os"
	"strings"
	"unicode/utf8"
)

type options struct {
	chars    bool
	bytes    bool
	words    bool
	lines    bool
	filename string
}

type results struct {
	chars int
	bytes int
	words int
	lines int
}

func new_results() results {
	return results{0, 0, 0, 0}
}

func main() {
	opts := options{false, false, false, false, ""}
	flag.BoolVar(&opts.chars, "m", false, "count number of chars")
	flag.BoolVar(&opts.bytes, "c", false, "count number of bytes")
	flag.BoolVar(&opts.words, "w", false, "count number of words")
	flag.BoolVar(&opts.lines, "l", false, "count number of lines")
	flag.Parse()

	if len(flag.Args()) > 0 {
		opts.filename = flag.Args()[0]
	}
	reader, readerSource := create_reader(opts.filename)
	defer readerSource.Close()

	counts := calculate(reader)

	print_results(opts, counts)
}

func create_reader(filename string) (reader *bufio.Reader, closer io.Closer) {
	readerSource := os.Stdin
	if filename != "" && !strings.HasPrefix(filename, "-") {
		file, err := os.Open(filename)
		if err != nil {
			log.Fatal(err)
		}
		readerSource = file
	}
	reader = bufio.NewReader(readerSource)
	return reader, readerSource
}

func calculate(reader *bufio.Reader) (counts results) {
	counts = new_results()

	var buf []byte
	var err error
	for err == nil {
		buf, err = reader.ReadBytes('\n')
		line := string(buf)

		counts.bytes += len(buf)
		counts.words += len(strings.Fields(line))
		counts.chars += utf8.RuneCountInString(line)
		if err != io.EOF {
			counts.lines += 1
		}
	}

	return counts
}

func print_results(opts options, counts results) {
	if *&opts.words {
		fmt.Printf("\t%d", counts.lines)
	}
	if *&opts.chars {
		fmt.Printf("\t%d", counts.chars)
	}
	if *&opts.bytes {
		fmt.Printf("\t%d", counts.bytes)
	}
	if *&opts.lines {
		fmt.Printf("\t%d", counts.lines)
	}
	if opts.filename != "" {
		fmt.Printf("\t%s", opts.filename)
	}
	fmt.Printf("\n")
}
