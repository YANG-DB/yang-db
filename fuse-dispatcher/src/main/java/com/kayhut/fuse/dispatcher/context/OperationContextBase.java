package com.kayhut.fuse.dispatcher.context;

/**
 * Created by User on 08/03/2017.
 */
public abstract class OperationContextBase<T extends OperationContextBase> implements OperationContext {
    //region Operation Context Implementation
    @Override
    public boolean isComplete() {
        return this.isComplete;
    }
    //endregion

    //region Public Methods
    public T complete() {
        OperationContextBase clone = this.cloneImpl();
        clone.isComplete = true;
        return (T)clone;
    }
    //endregion

    //region Abstract Methods
    protected abstract T cloneImpl();
    //endregion

    //region Fields
    private boolean isComplete;
    //endregion
}
