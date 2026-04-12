package com.example.client;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.RechnungsmetadatenServiceGrpc;
import com.google.protobuf.Timestamp;
import io.github.cdimascio.dotenv.Dotenv;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RechnungClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RechnungClient.class);
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private final ManagedChannel channel;
    private final RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceBlockingStub stub;
    private final GrpcMapper grpcMapper;

    public RechnungClient() {
        this.channel = ManagedChannelBuilder.forAddress(
                        dotenv.get("GRPC_HOST"),
                        Integer.parseInt(dotenv.get("GRPC_PORT")))
                .usePlaintext().build();
        this.stub = RechnungsmetadatenServiceGrpc.newBlockingStub(channel);
        this.grpcMapper = new GrpcMapper();
    }

    public RechnungsMetadata.APIResponse speichereRechnungsMetadaten(Rechnung rechnung) {
        var now = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                .build();
        var faelligkeitsdatum = toTimestamp(rechnung.getFaelligkeitsdatum());

        var request = RechnungsMetadata.MetadatenSpeichernRequest.newBuilder()
                .setRechnungsnummer(rechnung.getRechnungsnummer())
                .setRechnungsausteller(rechnung.getAussteller())
                .setRechnungsempfaenger(rechnung.getEmpfaenger())
                .setRechnungsdatum(now)
                .setFaelligkeitsdatum(faelligkeitsdatum)
                .setIban(rechnung.getIban())
                .setBic(rechnung.getBic())
                .addAllPositionen(rechnung.getPositionen().stream().map(grpcMapper::mapToGrpc).toList())
                .build();

        return stub.speicherMetadaten(request);
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(java.time.ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }

    @Override
    public void close() throws Exception {
        logger.info("Schließe gRPC-Verbindung...");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
