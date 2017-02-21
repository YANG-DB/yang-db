package com.kayhut.fuse.model.process;

/**
 * Created by lior on 20/02/2017.
 */
public interface ProcessElement<IN,OUT> {
    OUT process(IN input);

}
