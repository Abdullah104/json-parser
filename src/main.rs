use std::{fs::read_dir, io::Read};

fn main() {
    let root_test_directory = read_dir("tests").unwrap();

    for root_test_directory_result in root_test_directory {
        let test_directory = read_dir(root_test_directory_result.unwrap().path()).unwrap();

        for test_result in test_directory {
            let mut test = std::fs::File::open(test_result.unwrap().path()).unwrap();
            let mut raw_json = String::new();

            test.read_to_string(&mut raw_json).unwrap();

            println!("{}", raw_json);
        }
    }
}
