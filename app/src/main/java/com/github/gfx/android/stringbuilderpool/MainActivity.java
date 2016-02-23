package com.github.gfx.android.stringbuilderpool;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// on Xperia A (Android 4.2.2)
// D/XXX: SimplePool: 294ms
// D/XXX: SynchronizedPool: 341ms
// D/XXX: No pool: 382ms
//
// on Xperia Z4 (Android 5.0.2)
// D/XXX: SimplePool: 152ms
// D/XXX: SynchronizedPool: 153m
// D/XXX: No pool: 116ms
public class MainActivity extends AppCompatActivity {

    int N = 100000;

    Pools.SimplePool<StringBuilder> simplePool = new Pools.SimplePool<>(10);

    Pools.SynchronizedPool<StringBuilder> synchronizedPool = new Pools.SynchronizedPool<>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                f();
            }
        });
    }

    void f() {
        System.gc();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            StringBuilder sb = obtain(simplePool);

            sb.append('"');
            sb.append("foo");
            sb.append('"');
            sb.append('.');
            sb.append('"');
            sb.append("foo");
            sb.append('"');

            simplePool.release(sb);
        }
        Log.d("XXX", "SimplePool: " + (System.currentTimeMillis() - t0) + "ms");

        System.gc();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            StringBuilder sb = obtain(synchronizedPool);

            sb.append('"');
            sb.append("foo");
            sb.append('"');
            sb.append('.');
            sb.append('"');
            sb.append("foo");
            sb.append('"');

            synchronizedPool.release(sb);
        }
        Log.d("XXX", "SynchronizedPool: " + (System.currentTimeMillis() - t0) + "ms");

        System.gc();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            StringBuilder sb = nonPoolObtain();

            sb.append('"');
            sb.append("foo");
            sb.append('"');
            sb.append('.');
            sb.append('"');
            sb.append("foo");
            sb.append('"');
        }
        Log.d("XXX", "No pool: " + (System.currentTimeMillis() - t0) + "ms");
    }

    @NonNull
    static StringBuilder nonPoolObtain() {
        return new StringBuilder();
    }

    @NonNull
    static StringBuilder obtain(Pools.Pool<StringBuilder> pool) {
        StringBuilder sb = pool.acquire();
        if (sb == null) {
            return new StringBuilder();
        }
        return sb;
    }
}
