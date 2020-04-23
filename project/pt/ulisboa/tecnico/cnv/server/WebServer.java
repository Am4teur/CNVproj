package pt.ulisboa.tecnico.cnv.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import BIT.*;


public class WebServer {

	public static void main(final String[] args) throws Exception {

		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);

		final HttpServer server = HttpServer.create(new InetSocketAddress( 8000), 0);


		server.createContext("/", new Hello());
		server.createContext("/sudoku", new MyHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("Server started at: " + server.getAddress().toString());
	}

	public static String parseRequestBody(InputStream is) throws IOException {
        InputStreamReader isr =  new InputStreamReader(is,"utf-8");
        BufferedReader br = new BufferedReader(isr);

        // From now on, the right way of moving from bytes to utf-8 characters:

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);

        }

        br.close();
        isr.close();

        return buf.toString();
    }
	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {
			long thread_id = Thread.currentThread().getId();
//			ICount.clearCounters(thread_id); // fixme Icount
//			StatisticsTool.clear_dyn_counters(thread_id); // fixme StatisticsTool -Dyn
//			StatisticsTool.clearAllocCounter(thread_id); // fixme StatisticsTool -Alloc
//			StatisticsTool.clear_LSCounter(thread_id); // fixme StatisticsTool -LS
			StatisticsTool.clearBranchCounter(thread_id); // fixme StatisticsTool -branch


			// Get the query.
			final String query = t.getRequestURI().getQuery();
			System.out.println("> Query:\t" + query);

			// Break it down into String[].
			final String[] params = query.split("&");

			// Store as if it was a direct call to SolverMain.
			final ArrayList<String> newArgs = new ArrayList<>();
			for (final String p : params) {
				final String[] splitParam = p.split("=");
				newArgs.add("-" + splitParam[0]);
				newArgs.add(splitParam[1]);
			}
			newArgs.add("-b");
			newArgs.add(parseRequestBody(t.getRequestBody()));

			newArgs.add("-d");

			// Store from ArrayList into regular String[].
			final String[] args = new String[newArgs.size()];
			int i = 0;
			for(String arg: newArgs) {
				args[i] = arg;
				i++;
			}
			System.out.println("reofroe0");

			// Get user-provided flags.
			final SolverArgumentParser ap = new SolverArgumentParser(args);


			System.out.println("reofroe1");

			// Create solver instance from factory.
			final Solver s = SolverFactory.getInstance().makeSolver(ap);
			System.out.println("reofroe2");

			//Solve sudoku puzzle
			JSONArray solution = s.solveSudoku();
			System.out.println("reofroe3");


			BufferedWriter out = null;

			try {
				File file = new File("tempBD.txt");
				FileWriter fstream = new FileWriter(file, true); //true tells to append data.
				out = new BufferedWriter(fstream);
				System.out.println("PRINT TO FILE");
				out.write("\n");
				out.write("> Query:\t" + query + "\n");

//				out.write("Number of methods:      " + StatisticsTool.get_dyn_method_count(thread_id) + "\n"); // fixme StatisticsTool -dym
//				out.write("Number of basic blocks: " + StatisticsTool.get_dyn_bb_count(thread_id) + "\n"); // fixme StatisticsTool -dym
//				out.write("Number of instructions: " + StatisticsTool.get_dyn_instr_count(thread_id) + "\n"); // fixme StatisticsTool -dym

//				out.write("Allocations summary:\n");
//				out.write("new:      " + StatisticsTool.getAllocCount_newcount(thread_id) + "\n"); // fixme StatisticsTool -Alloc
//				out.write("newarray: " + StatisticsTool.getAllocCount_newarraycount(thread_id) + "\n"); // fixme StatisticsTool -Alloc
//				out.write("anewarray: " + StatisticsTool.getAllocCount_anewarraycount(thread_id) + "\n"); // fixme StatisticsTool -Alloc
//				out.write("multianewarray: " + StatisticsTool.getAllocCount_multianewarraycount(thread_id) + "\n"); // fixme StatisticsTool -Alloc

//				out.write("Load Store Summary:\n");
//				out.write("Field load:      " + StatisticsTool.getLSCount_fieldloadcount(thread_id) + "\n"); // fixme StatisticsTool -LS
//				out.write("Field store: " + StatisticsTool.getLSCount_fieldstorecount(thread_id) + "\n"); // fixme StatisticsTool -LS
//				out.write("Regular load: " + StatisticsTool.getLSCount_loadcount(thread_id) + "\n"); // fixme StatisticsTool -LS
//				out.write("Regular store: " + StatisticsTool.getLSCount_storecount(thread_id) + "\n"); // fixme StatisticsTool -LS

				out.write("Taken: " + StatisticsTool.getTakenCounter(thread_id) + "\n"); // fixme StatisticsTool -branch
				out.write("Not Taken: " + StatisticsTool.getNotTakenCounter(thread_id) + "\n"); // fixme StatisticsTool -branch

//				out.write("> ICounter:\t" + ICount.getICount(thread_id) + "\n"); // fixme Icount
//				out.write("> BCounter:\t" + ICount.getBCount(thread_id) + "\n");// fixme Icount
//				out.write("> MCounter:\t" + ICount.getMCount(thread_id) + "\n");// fixme Icount
				out.write("\n");





				System.out.println("PRINT TO FILE DONE");
			}
			
			catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
			}
			
			finally {
				if(out != null) {
					out.close();
				}
			}


			// Send response to browser.
			final Headers hdrs = t.getResponseHeaders();

            //t.sendResponseHeaders(200, responseFile.length());


			///hdrs.add("Content-Type", "image/png");
            hdrs.add("Content-Type", "application/json");

			hdrs.add("Access-Control-Allow-Origin", "*");

            hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            t.sendResponseHeaders(200, solution.toString().length());


            final OutputStream os = t.getResponseBody();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(solution.toString());
            osw.flush();
            osw.close();

			os.close();

			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
		}
	}
	static class Hello implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "Hello! I'm up and running!";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
