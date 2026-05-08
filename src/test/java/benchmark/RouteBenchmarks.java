package benchmark;

import algorithm.BFS;
import algorithm.DFS;
import algorithm.Dijkstra;
import model.Room;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import service.RouteService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Basic JMH benchmarks for the core route-finding methods.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class RouteBenchmarks {

    private RouteService routeService;
    private DFS dfs;
    private BFS bfs;
    private Dijkstra dijkstra;
    private Room startRoom;
    private Room endRoom;
    private Room waypointRoom;
    private Room avoidRoom;
    private List<String> preferredArtists;

    @Setup(Level.Trial)
    public void setUp() {
        routeService = new RouteService();
        dfs = new DFS();
        bfs = new BFS();
        dijkstra = new Dijkstra();
        startRoom = routeService.findRoomById("2");
        endRoom = routeService.findRoomById("46");
        waypointRoom = routeService.findRoomById("14");
        avoidRoom = routeService.findRoomById("12");
        preferredArtists = List.of("Titian", "Monet");
    }

    @Benchmark
    public Object benchmarkAllPathsDfs() {
        return dfs.findAllPaths(startRoom, endRoom, 20);
    }

    @Benchmark
    public Object benchmarkShortestPathBfs() {
        return bfs.findShortestPath(startRoom, endRoom);
    }

    @Benchmark
    public Object benchmarkShortestPathDijkstra() {
        return dijkstra.findShortestPath(startRoom, endRoom);
    }

    @Benchmark
    public Object benchmarkInterestingPathDijkstra() {
        return dijkstra.findMostInterestingPath(startRoom, endRoom);
    }

    @Benchmark
    public Object benchmarkServiceShortestPathWithWaypoint() {
        return routeService.getShortestRouteDijkstra(startRoom, endRoom, List.of(waypointRoom), List.of());
    }

    @Benchmark
    public Object benchmarkServiceInterestingPathWithAvoid() {
        return routeService.getInterestingRoute(startRoom, endRoom, preferredArtists, List.of(), List.of(avoidRoom));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(RouteBenchmarks.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
