package com.novoda.tpbot.human;

import android.os.Handler;
import android.os.Looper;

import com.novoda.tpbot.Direction;
import com.novoda.tpbot.Result;
import com.novoda.tpbot.support.Observable;

import java.net.URI;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class HumanSocketIOTpService implements HumanTpService {

    private final Socket socket;
    private final Handler handler;

    public static HumanSocketIOTpService getInstance() {
        return LazySingleton.INSTANCE;
    }

    private HumanSocketIOTpService() {
        this.socket = IO.socket(URI.create("http://192.168.86.158:3000"));
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Observable<Result> connect() {
        return new SocketConnectionObservable();
    }

    private class SocketConnectionObservable extends Observable<Result> {

        @Override
        public Observable<Result> start() {
            if (socket.connected()) {
                notifyOf(Result.from("Already connected"));
                return this;
            }

            socket.connect();
            socket.emit("join_as_human", "", new Ack() {
                @Override
                public void call(final Object... args) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (args[0] != null) {
                                Object object = args[0];
                                notifyOf(Result.from(object.toString()));
                            }
                        }
                    });
                }
            });
            return this;
        }
    }

    @Override
    public void moveIn(Direction direction) {
        socket.emit("move_in", direction);
    }

    @Override
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
    }

    private static class LazySingleton {
        private static final HumanSocketIOTpService INSTANCE = new HumanSocketIOTpService();
    }
}
