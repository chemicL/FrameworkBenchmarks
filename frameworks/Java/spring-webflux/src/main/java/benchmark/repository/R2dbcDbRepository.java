package benchmark.repository;

import java.util.concurrent.Future;
import java.util.function.Function;

import benchmark.model.Fortune;
import benchmark.model.World;
import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DataClassRowMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class R2dbcDbRepository implements DbRepository {
    private final DatabaseClient databaseClient;

    private final ThreadLocal<Mono<? extends Connection>> conn;

    public R2dbcDbRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        this.conn = new ThreadLocal<>();
    }

    private Mono<? extends Connection> getConnection() {
        if (this.conn.get() == null) {
            this.conn.set(Mono.from(databaseClient.getConnectionFactory().create()).cache());
        }
        return this.conn.get();
    }
    @Override
    public Mono<World> getWorld(int id) {
        return databaseClient
                .sql("SELECT id, randomnumber FROM world WHERE id = $1")
                .bind("$1", id)
                .mapProperties(World.class)
                .first();

    }

    public Mono<World> updateWorld(World world) {
        return databaseClient
                .sql("UPDATE world SET randomnumber=$2 WHERE id = $1")
                .bind("$1", world.id)
                .bind("$2", world.randomnumber)
                .fetch()
                .rowsUpdated()
                .map(count -> world);
    }

    public Mono<World> findAndUpdateWorld(int id, int randomNumber) {
        return getWorld(id).flatMap(world -> {
            world.randomnumber = randomNumber;
            return updateWorld(world);
        });
    }

    @Override
    public Flux<Fortune> fortunes() {
        Flux<? extends Result> results = getConnection()
                .flatMapMany(conn -> Flux.from(
                        conn.createStatement("SELECT id, message FROM " + "fortune").execute()));
        return results
                .flatMap(result -> result.map(new DataClassRowMapper<>(Fortune.class)));
    }
}