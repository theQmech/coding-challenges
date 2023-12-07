use clap::builder::StringValueParser;
use clap::{Arg, Command};
use std::fs::File;
use std::io::{self, BufRead, BufReader};

// #[derive(Parser)]
struct Cli {
    fields: Vec<usize>,
    delimiter: char,
    reader: Box<dyn BufRead>,
}

fn parse_params() -> Cli {
    let matches = Command::new("cccut")
        .arg(
            Arg::new("fields")
                .short('f')
                .long("fields")
                .value_parser(StringValueParser::new())
                .help("select fields by index starting at 1"),
        )
        .arg(
            Arg::new("delimiter")
                .short('d')
                .long("delimier")
                .required(false)
                .default_value("\t")
                .help("delimiter character, defaults to tab"),
        )
        .arg(
            Arg::new("path")
                .required(false)
                .default_value("-")
                .help("path to source file (reads from stdin if none provided)"),
        )
        .get_matches();

    Cli {
        fields: matches
            .get_one::<String>("fields")
            .expect("cannot parse argument <fields>")
            .split(",")
            .map(|s| s.trim().parse().unwrap_or_else(|_| panic!("here {}", s)))
            .collect::<Vec<_>>(),
        delimiter: matches
            .get_one::<String>("delimiter")
            .expect("cannot parse argument <delimiter>")
            .chars()
            .take(1)
            .last()
            .unwrap(),
        reader: match matches.get_one::<String>("path") {
            None => Box::new(BufReader::new(io::stdin())),
            Some(path) => {
                if path == "-" {
                    Box::new(BufReader::new(io::stdin()))
                } else {
                    Box::new(BufReader::new(File::open(path).unwrap()))
                }
            }
        },
    }
}

// // The output is wrapped in a Result to allow matching on errors
// // Returns an Iterator to the Reader of the lines of the file.
// fn read_lines<P>(filename: P) -> io::Result<io::Lines<io::BufReader<File>>>
// where
//     P: AsRef<Path>,
// {
//     let file = File::open(filename)?;
//     Ok(io::BufReader::new(file).lines())
// }

fn run_cut(params: Cli, mut writer: impl io::Write) {
    for result_line in params.reader.lines() {
        match result_line {
            Ok(line) => {
                let result = params
                    .fields
                    .iter()
                    .map(|idx| {
                        line.split(params.delimiter)
                            .nth(idx - 1)
                            .expect("Field not found. File contents might have changed recently.")
                            .to_string()
                    })
                    .collect::<Vec<String>>()
                    .join(&params.delimiter.to_string());
                writeln!(writer, "{}", result).expect("cannot write output");
            }
            Err(_) => panic!("Error reading input"),
        }
    }
}

fn main() {
    let params = parse_params();

    run_cut(params, &mut std::io::stdout());
}

#[cfg(test)]
mod tests {
    use assert_cmd::Command;

    fn get_command() -> Command {
        Command::cargo_bin("cccust").expect("program not found")
    }

    #[test]
    fn check_field_number() {
        let expected = "f1\n1\n6\n11\n16\n21\n";
        get_command()
            .arg("-f2")
            .arg("sample.tsv")
            .assert()
            .success()
            .code(0)
            .stdout(expected);
    }

    #[test]
    fn check_field_numbers_comma_separated() {
        let expected = "f0\tf1\n0\t1\n5\t6\n10\t11\n15\t16\n20\t21\n";
        get_command()
            .arg("-f1,2")
            .arg("sample.tsv")
            .assert()
            .success()
            .code(0)
            .stdout(expected);
    }

    #[test]
    fn check_delimiter() {
        let expected =
            "\u{feff}Song title\n\"10000 Reasons (Bless the Lord)\"\n\"20 Good Reasons\"\n\"Adore You\"\n\"Africa\"";
        get_command()
            .arg("-f1")
            .arg("-d,")
            .arg("fourchords.csv")
            .assert()
            .success()
            .code(0)
            .stdout(predicates::str::starts_with(expected));
    }

    #[test]
    fn check_read_from_stdin_when_no_path_provided() {
        let input_str = "abc,def\nxyz,pqr\n";
        let expected = "abc\nxyz\n";
        get_command()
            .arg("-f1")
            .arg("-d,")
            .write_stdin(input_str)
            .assert()
            .success()
            .code(0)
            .stdout(expected);
    }

    #[test]
    fn check_read_from_stdin_when_path_is_hyphen() {
        let expected = "f0\n0\n5\n10\n15\n20\n";
        get_command()
            .arg("-f1")
            .pipe_stdin("sample.tsv")
            .expect("error piping input file")
            .assert()
            .success()
            .code(0)
            .stdout(predicates::str::starts_with(expected));
    }
}
