package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.log.Trace;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q>{
    ValidationContext isPlanValid(P plan, Q query);
}
