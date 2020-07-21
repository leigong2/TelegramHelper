package com.telegram.helper.util;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class SimpleObserver<T, V> implements Observer<T> {
    public int position;
    V v;
    boolean control;

    public SimpleObserver(V v, boolean control) {
        this.v = v;
        this.control = control;
    }

    public SimpleObserver(V v, boolean control, int position) {
        this.v = v;
        this.control = control;
        this.position = position;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onNext(t, v);
    }

    public abstract void onNext(T t, V v);

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
