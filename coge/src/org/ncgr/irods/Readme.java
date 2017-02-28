package org.ncgr.irods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Provides methods for extracting useful chunks of info from an LIS README.md file.
 */
public class Readme {

    File file;

    /**
     * Construct from a file.
     */
    public Readme(File file) throws FileNotFoundException {
        this.file = file;
    }

    /**
     * the genotype
     */
    public String getGenotype() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line=reader.readLine())!=null) {
            if (line.startsWith("#### Genotype")) {
                String comment = reader.readLine();
                return reader.readLine();
            }
        }
        return null;
    }

    /**
     * the provenance
     */
    public String getProvenance() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line=reader.readLine())!=null) {
            if (line.startsWith("#### Provenance")) {
                String comment = reader.readLine();
                return reader.readLine();
            }
        }
        return null;
    }

    /**
     * the identifier
     */
    public String getIdentifier() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line=reader.readLine())!=null) {
            if (line.startsWith("#### Identifier")) {
                String comment = reader.readLine();
                return reader.readLine();
            }
        }
        return null;
    }

    /**
     * the source
     */
    public String getSource() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line=reader.readLine())!=null) {
            if (line.startsWith("#### Source")) {
                String comment = reader.readLine();
                return reader.readLine();
            }
        }
        return null;
    }

}
