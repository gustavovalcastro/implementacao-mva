import java.util.Arrays;
import java.util.Scanner;


public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
        int opt = 1;
		while (opt == 1) {
			System.out.println("\n--------------------------------------------------");
			System.out.println("Algoritmo MVA:");
			System.out.println("--------------------------------------------------");
			System.out.println("Autores:");
			System.out.println("Gustavo Valadares Castro");
			System.out.println("Maicon Gomes Messias");
			System.out.println("Samuel Vieira Lobo Chiodi");
			System.out.println("--------------------------------------------------");
			Config configuracao = getEntradas(scanner);
			Saida resultados = execMva(configuracao);
			printResultados(resultados);
			System.out.println("--------------------------------------------------");
			System.out.println("Continuar [1]. Encerrar [0].");
            opt = scanner.nextInt();
		}
	}

	static Config getEntradas(Scanner scanner) {
		System.out.println("Entradas:");
        System.out.println("--------------------------------------------------");
		
		System.out.print("Número de filas (recursos): ");
		int numRecursos = scanner.nextInt();
		
		int[] vi = new int[numRecursos];
		double[] si = new double[numRecursos];


		for (int i = 0; i < numRecursos; i++) {
			System.out.printf("Taxa de chegada do recurso %d: ", i + 1);
			vi[i] = scanner.nextInt();
		}

		for (int i = 0; i < numRecursos; i++) {
			System.out.printf("Taxa de serviço do recurso %d: ", i + 1);
			si[i] = scanner.nextDouble();
		}

		System.out.print("Número de clientes: ");
		int numClientes = scanner.nextInt();

		return new Config(numRecursos, vi, si, numClientes);
	}

	static Saida execMva(Config config) {
		double[] ni = new double[config.numRecursos];
		Arrays.fill(ni, 0.0);

		double x0 = 0.0;
		double[] xi = new double[config.numRecursos];

		TempoMedioResposta r = new TempoMedioResposta();

		for (int clientes = 1; clientes <= config.numClientes; clientes++) {
			r = getTempoRespostaSistema(config.si, ni, config.vi);
			x0 = getThroughputSistema(clientes, r.getR0());
			xi = getThroughputDispositivos(config.vi, x0);
			ni = getNumeroMedioClientesDispositos(r.getRi(), xi);
		}

		double[] w = getTempoMedioEsperaDispositivos(r.getRi(), config.si);
		double[] u = getUtilizacaoDispositivos(config.si, xi);
		double[] n = ni.clone(); 

		double wSistema = getTempoMedioEsperaSistema(w);
		double uSistema = getUtilizacaoTotal(u);
		double nSistema = getNumeroMedioClientesTotal(n);

		return new Saida(r.getR0(), w, u, n, wSistema, uSistema, nSistema);
	}

	static void printResultados(Saida resultados) {
		System.out.println("\nResultados:");
		System.out.println("--------------------------------------------------");
		System.out.printf("- Tempo médio de resposta total (R) = %.2f\n", resultados.r);

		for (int i = 0; i < resultados.w.length; i++) {
			System.out.printf("\nDispositivo #%d\n", i + 1);
			System.out.printf("- Tempo médio de espera (W) = %.2f\n", resultados.w[i]);
			System.out.printf("- Utilização (U) = %.2f\n", resultados.u[i]);
			System.out.printf("- Número médio de clientes (L) = %.2f\n", resultados.n[i]);
		}

        System.out.println("\n--------------------------------------------------");
		System.out.println("Resultados do Sistema:");
        System.out.println("--------------------------------------------------");
		System.out.printf("- Tempo médio de espera do sistema (W) = %.2f\n", resultados.wSistema);
		System.out.printf("- Utilização total do servidor (U) = %.2f\n", resultados.uSistema);
		System.out.printf("- Número médio de clientes no sistema (L) = %.2f\n", resultados.nSistema);
	}

	static TempoMedioResposta getTempoRespostaSistema(double[] si, double[] ni, int[] vi) {
		double[] ri = new double[si.length];
		for (int i = 0; i < si.length; i++) {
			ri[i] = si[i] * (ni[i] + 1);
		}
		return new TempoMedioResposta(ri, vi);
	}

	static double getThroughputSistema(int clientes, double r0) {
		return clientes / r0;
	}

	static double[] getThroughputDispositivos(int[] vi, double x0) {
		double[] xi = new double[vi.length];
		for (int i = 0; i < vi.length; i++) {
			xi[i] = vi[i] * x0;
		}
		return xi;
	}

	static double[] getNumeroMedioClientesDispositos(double[] ri, double[] xi) {
		double[] ni = new double[ri.length];
		for (int i = 0; i < ri.length; i++) {
			ni[i] = ri[i] * xi[i];
		}
		return ni;
	}

	static double[] getTempoMedioEsperaDispositivos(double[] ri, double[] si) {
		double[] w = new double[ri.length];
		for (int i = 0; i < ri.length; i++) {
			w[i] = ri[i] - si[i];
		}
		return w;
	}


	static double getTempoMedioEsperaSistema(double[] w) {
		double totalW = 0.0;
		for (double wi : w) {
			totalW += wi;
		}
		return totalW / w.length;
	}

	static double[] getUtilizacaoDispositivos(double[] si, double[] xi) {
		double[] u = new double[si.length];
		for (int i = 0; i < si.length; i++) {
			u[i] = si[i] * xi[i];
		}
		return u;
	}

	static double getUtilizacaoTotal(double[] u) {
		double totalU = 0.0;
		for (double ui : u) {
			totalU += ui;
		}
		return totalU;
	}

	static double getNumeroMedioClientesTotal(double[] n) {
		double totalN = 0.0;
		for (double ni : n) {
			totalN += ni;
		}
		return totalN;
	}
}

class Config {
	int numRecursos;
	int[] vi;
	double[] si;
	int numClientes;

	Config(int numRecursos, int[] vi, double[] si, int numClientes) {
		this.numRecursos = numRecursos;
		this.vi = vi;
		this.si = si;
		this.numClientes = numClientes;
	}
}

class Saida {
	double r;
	double[] w, u, n;
	double wSistema, uSistema, nSistema;

	Saida(double r, double[] w, double[] u, double[] n, double wSistema, double uSistema, double nSistema) {
		this.r = r;
		this.w = w;
		this.u = u;
		this.n = n;
		this.wSistema = wSistema;
		this.uSistema = uSistema;
		this.nSistema = nSistema;
	}
}

class TempoMedioResposta {
	private double[] ri;
	private double r0;

	TempoMedioResposta() {
	}
	

	TempoMedioResposta(double[] ri, int[] vi) {
		this.ri = ri;
		for (int i = 0; i < ri.length; i++) {
			this.r0 += vi[i] * ri[i];
		}
	}

	double[] getRi() {
		return ri;
	}

	double getR0() {
		return r0;
	}
}
