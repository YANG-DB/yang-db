package com.kayhut.fuse.services.appRegistrars;

import com.google.inject.TypeLiteral;
import org.jooby.*;

public abstract class AppControllerRegistrarBase<TController> implements AppRegistrar{
    //region Constructors
    public AppControllerRegistrarBase(Class<? extends TController> klass) {
        this.klass = klass;
    }

    public AppControllerRegistrarBase(TypeLiteral<TController> typeLiteral) {
        this.typeLiteral = typeLiteral;
    }
    //endregion

    //region Protected Methods
    protected TController getController(Jooby app) {
        if (this.klass != null) {
            return app.require(this.klass);
        }

        if (this.typeLiteral != null) {
            return app.require(this.typeLiteral);
        }

        return null;
    }


    //endregion

    //region Fields
    private Class<? extends TController> klass;
    private TypeLiteral<TController> typeLiteral;
    //endregion
}
