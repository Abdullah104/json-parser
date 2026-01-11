package main

import (
	"fmt"
	"io"
	"log"
	"os"
	"path"
)

func main() {
	tDirs, err := os.ReadDir("tests")
	if err != nil {
		log.Fatal(err)
	}

	for index, tDir := range tDirs {
		if index > 0 {
			break
		}

		tFiles, err := os.ReadDir(path.Join("tests", tDir.Name()))
		if err != nil {
			log.Fatal(err)
		}

		for _, tFileEntry := range tFiles {
			file, err := os.Open(path.Join("tests", tDir.Name(), tFileEntry.Name()))
			if err != nil {
				log.Fatal(err)
			}

			defer file.Close()

			content, err := io.ReadAll(file)
			if err != nil {
				log.Fatal(err)
			}

			rawJson := string(content)

			fmt.Println(rawJson)
		}
	}
}
