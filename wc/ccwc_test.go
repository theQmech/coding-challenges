package main

import (
	"bufio"
	"fmt"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"testing"
)

func TestCharacterCount(t *testing.T) {
	file, err := os.Open("test.txt")
	if err != nil {
		panic(err)
	}
	defer file.Close()
	have := calculate(bufio.NewReader(file)).chars
	want := getShellOutput("wc", "-m", "test.txt")[0]
	if strconv.Itoa(have) != want {
		t.Fatalf(`Wanted [%v], got [%v]"`, want, have)
	}
}

func TestByteCount(t *testing.T) {
	file, err := os.Open("test.txt")
	if err != nil {
		panic(err)
	}
	defer file.Close()
	have := calculate(bufio.NewReader(file)).bytes
	want := getShellOutput("wc", "-c", "test.txt")[0]
	if strconv.Itoa(have) != want {
		t.Fatalf(`Wanted [%v], got [%v]"`, want, have)
	}
}

func TestWordCount(t *testing.T) {
	file, err := os.Open("test.txt")
	if err != nil {
		panic(err)
	}
	defer file.Close()
	have := calculate(bufio.NewReader(file)).words
	want := getShellOutput("wc", "-w", "test.txt")[0]
	if strconv.Itoa(have) != want {
		t.Fatalf(`Wanted [%v], got [%v]"`, want, have)
	}
}

func TestLineCount(t *testing.T) {
	file, err := os.Open("test.txt")
	if err != nil {
		panic(err)
	}
	defer file.Close()
	have := calculate(bufio.NewReader(file)).lines
	want := getShellOutput("wc", "-l", "test.txt")[0]
	if strconv.Itoa(have) != want {
		t.Fatalf(`Wanted [%v], got [%v]"`, want, have)
	}
}

func getShellOutput(command string, args ...string) []string {
	output, err := exec.Command(command, args...).Output()
	if err != nil {
		fmt.Printf("Failed shell command [%v, %v] [%v]\n", command, args, output)
		panic(err)
	}
	return strings.Fields(string(output))
}
