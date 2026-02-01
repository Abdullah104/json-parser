mod json_parser;
mod json_value;
mod pair;
mod tokens;

use std::{
    fs::{File, read_dir},
    io::Read,
};

use json_parser::JsonParser;

fn main() {
    let root_test_directory = read_dir("tests").unwrap();

    for test_step_directory_result in root_test_directory {
        let step_path = test_step_directory_result.unwrap().path();
        if !step_path.is_dir() {
            continue;
        }

        // if step_path.to_str().unwrap().contains("1") {
        //     continue;
        // }

        // if step_path.to_str().unwrap().contains("2") {
        //     continue;
        // }

        // if step_path.to_str().unwrap().contains("3") {
        //     continue;
        // }

        // if step_path.to_str().unwrap().contains("4") {
        //     continue;
        // }

        // if step_path.to_str().unwrap().contains("5") {
        //     continue;
        // }

        let step_directory = read_dir(&step_path).unwrap();

        for test_result in step_directory {
            let test_path = test_result.unwrap().path();
            let mut test = File::open(&test_path).unwrap();
            let mut raw_json = String::new();

            test.read_to_string(&mut raw_json).unwrap();

            let json_parser = JsonParser::new(raw_json);

            print!("{}: ", test_path.to_str().unwrap());
            match json_parser.parse() {
                Some(json) => println!("{:#?}", json),
                None => println!("Invalid json format"),
            }
        }
    }
}
