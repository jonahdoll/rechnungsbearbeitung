package com.example.camundaworkers;

import com.example.grpc.RechnungsmetadatenServiceGrpc;
import io.github.cdimascio.dotenv.Dotenv;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient implements AutoCloseable {

  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

  private final ManagedChannel channel;
  private final RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceBlockingStub stub;

  public GrpcClient() {
    this(dotenv.get("GRPC_HOST"), Integer.parseInt(dotenv.get("GRPC_PORT")));
  }

  public GrpcClient(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    this.stub = RechnungsmetadatenServiceGrpc.newBlockingStub(channel);
  }

  public RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceBlockingStub getStub() {
    return stub;
  }

  @Override
  public void close() {
    channel.shutdown();
  }
}
