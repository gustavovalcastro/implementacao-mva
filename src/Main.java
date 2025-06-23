import java.util.Arrays;
import java.util.Scanner;


public class Main {
	/**
	 * Método principal que executa o algoritmo MVA (Mean Value Analysis)
	 * Controla o fluxo principal do programa, permitindo múltiplas execuções
	 * @param args argumentos da linha de comando (não utilizados)
	 */
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

	/**
	 * Coleta os dados de entrada do usuário para o algoritmo MVA
	 * @param scanner objeto Scanner para leitura de entrada do usuário
	 * @return objeto Config contendo todos os parâmetros necessários para o MVA
	 */
	static Config getEntradas(Scanner scanner) {
		System.out.println("Entradas:");
        System.out.println("--------------------------------------------------");
		
		// Número de recursos (filas) no sistema
		System.out.print("Número de filas (recursos): ");
		int numRecursos = scanner.nextInt();
		
		// Arrays para armazenar taxas de visita e taxas de serviço
		//int[] vi = new double[numRecursos];
	    double[] vi = new double[numRecursos];
		double[] si = new double[numRecursos];

		// Coleta as taxas de visita para cada recurso
		for (int i = 0; i < numRecursos; i++) {
			System.out.printf("Taxa de chegada do recurso %d: ", i + 1);
			//vi[i] = scanner.nextInt();
			vi[i] = scanner.nextDouble();
		}

		// Coleta as taxas de serviço para cada recurso
		for (int i = 0; i < numRecursos; i++) {
			System.out.printf("Taxa de serviço do recurso %d: ", i + 1);
			si[i] = scanner.nextDouble();
		}

		// Número total de clientes no sistema fechado
		System.out.print("Número de clientes: ");
		int numClientes = scanner.nextInt();

		return new Config(numRecursos, vi, si, numClientes);
	}

	/**
	 * Executa o algoritmo MVA (Mean Value Analysis) para análise de desempenho
	 * Implementa o algoritmo iterativo que calcula métricas de desempenho para
	 * sistemas de filas fechados com múltiplos recursos
	 * @param config configuração do sistema contendo parâmetros de entrada
	 * @return objeto Saida com todas as métricas calculadas
	 */
	static Saida execMva(Config config) {
		// Inicializa número médio de clientes em cada dispositivo (começando com 0)
		double[] ni = new double[config.numRecursos];
		Arrays.fill(ni, 0.0);

		// Variáveis para throughput do sistema e dos dispositivos
		double x0 = 0.0;
		double[] xi = new double[config.numRecursos];

		// Objeto para armazenar tempos de resposta
		TempoMedioResposta r = new TempoMedioResposta();

		for (int clientes = 1; clientes <= config.numClientes; clientes++) {
			r = getTempoRespostaSistema(config.si, ni, config.vi);
			x0 = getThroughputSistema(clientes, r.getR0());
			xi = getThroughputDispositivos(config.vi, x0);
			ni = getNumeroMedioClientesDispositos(r.getRi(), xi);
		}

		// Calcula métricas finais do sistema
		double[] w = getTempoMedioEsperaDispositivos(r.getRi(), config.si);
		double[] u = getUtilizacaoDispositivos(config.si, xi);
		double[] n = ni.clone(); 

		// Métricas agregadas do sistema
		double wSistema = getTempoMedioEsperaSistema(w);
		double uSistema = getUtilizacaoTotal(u);
		double nSistema = getNumeroMedioClientesTotal(n);

		return new Saida(r.getR0(), w, u, n, wSistema, uSistema, nSistema);
	}

	/**
	 * Exibe os resultados calculados pelo algoritmo MVA de forma formatada
	 * Apresenta métricas individuais por dispositivo e métricas agregadas do sistema
	 * @param resultados objeto contendo todas as métricas calculadas
	 */
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

	/**
	 * Calcula o tempo médio de resposta para cada dispositivo e do sistema
	 * Implementa a fórmula fundamental do MVA: Ri(n) = Si * (Ni(n-1) + 1)
	 * @param si array com tempos de serviço de cada dispositivo
	 * @param ni array com número médio de clientes em cada dispositivo (estado anterior)
	 * @param vi array com taxas de visita de cada dispositivo
	 * @return objeto TempoMedioResposta contendo Ri e R0
	 */
	static TempoMedioResposta getTempoRespostaSistema(double[] si, double[] ni, double[] vi) {
		double[] ri = new double[si.length];
		// Aplica a fórmula do MVA para cada dispositivo
		for (int i = 0; i < si.length; i++) {
			ri[i] = si[i] * (ni[i] + 1);
		}
		return new TempoMedioResposta(ri, vi);
	}

	/**
	 * Calcula o throughput do sistema usando a Lei de Little
	 * Fórmula: X0 = N / R0 (número de clientes dividido pelo tempo de resposta total)
	 * @param clientes número atual de clientes no sistema
	 * @param r0 tempo médio de resposta total do sistema
	 * @return throughput do sistema (clientes/unidade de tempo)
	 */
	static double getThroughputSistema(int clientes, double r0) {
		return clientes / r0;
	}

	/**
	 * Calcula o throughput individual de cada dispositivo
	 * Fórmula: Xi = Vi * X0 (taxa de visita multiplicada pelo throughput do sistema)
	 * @param vi array com taxas de visita de cada dispositivo
	 * @param x0 throughput do sistema
	 * @return array com throughput de cada dispositivo
	 */
	static double[] getThroughputDispositivos(double[] vi, double x0) {
		double[] xi = new double[vi.length];
		for (int i = 0; i < vi.length; i++) {
			xi[i] = vi[i] * x0;
		}
		return xi;
	}

	/**
	 * Calcula o número médio de clientes em cada dispositivo usando Lei de Little
	 * Fórmula: Ni = Ri * Xi (tempo de resposta multiplicado pelo throughput)
	 * @param ri array com tempos de resposta de cada dispositivo
	 * @param xi array com throughput de cada dispositivo
	 * @return array com número médio de clientes em cada dispositivo
	 */
	static double[] getNumeroMedioClientesDispositos(double[] ri, double[] xi) {
		double[] ni = new double[ri.length];
		for (int i = 0; i < ri.length; i++) {
			ni[i] = ri[i] * xi[i];
		}
		return ni;
	}

	/**
	 * Calcula o tempo médio de espera em cada dispositivo
	 * Fórmula: Wi = Ri - Si (tempo de resposta menos tempo de serviço)
	 * @param ri array com tempos de resposta de cada dispositivo
	 * @param si array com tempos de serviço de cada dispositivo
	 * @return array com tempos médios de espera de cada dispositivo
	 */
	static double[] getTempoMedioEsperaDispositivos(double[] ri, double[] si) {
		double[] w = new double[ri.length];
		for (int i = 0; i < ri.length; i++) {
			w[i] = ri[i] - si[i];
		}
		return w;
	}


	/**
	 * Calcula o tempo médio de espera agregado do sistema
	 * Média aritmética dos tempos de espera de todos os dispositivos
	 * @param w array com tempos médios de espera de cada dispositivo
	 * @return tempo médio de espera do sistema completo
	 */
	static double getTempoMedioEsperaSistema(double[] w) {
		double totalW = 0.0;
		for (double wi : w) {
			totalW += wi;
		}
		return totalW / w.length;
	}

	/**
	 * Calcula a utilização de cada dispositivo
	 * Fórmula: Ui = Si * Xi (tempo de serviço multiplicado pelo throughput)
	 * Representa a fração de tempo que cada dispositivo está ocupado
	 * @param si array com tempos de serviço de cada dispositivo
	 * @param xi array com throughput de cada dispositivo
	 * @return array com utilização de cada dispositivo (0 a 1)
	 */
	static double[] getUtilizacaoDispositivos(double[] si, double[] xi) {
		double[] u = new double[si.length];
		for (int i = 0; i < si.length; i++) {
			u[i] = si[i] * xi[i];
		}
		return u;
	}

	/**
	 * Calcula a utilização total do sistema
	 * Soma das utilizações de todos os dispositivos
	 * @param u array com utilização de cada dispositivo
	 * @return utilização total do sistema
	 */
	static double getUtilizacaoTotal(double[] u) {
		double totalU = 0.0;
		for (double ui : u) {
			totalU += ui;
		}
		return totalU;
	}

	/**
	 * Calcula o número médio total de clientes no sistema
	 * Soma do número médio de clientes em todos os dispositivos
	 * @param n array com número médio de clientes em cada dispositivo
	 * @return número médio total de clientes no sistema
	 */
	static double getNumeroMedioClientesTotal(double[] n) {
		double totalN = 0.0;
		for (double ni : n) {
			totalN += ni;
		}
		return totalN;
	}
}

/**
 * Classe para armazenar a configuração de entrada do sistema MVA
 * Contém todos os parâmetros necessários para executar o algoritmo
 */
class Config {
	int numRecursos;    // Número de dispositivos/recursos no sistema
	double[] vi;           // Array com taxas de visita de cada recurso
	double[] si;        // Array com tempos de serviço de cada recurso
	int numClientes;    // Número total de clientes no sistema fechado

	/**
	 * Construtor da classe Config
	 * @param numRecursos número de recursos no sistema
	 * @param vi array com taxas de visita
	 * @param si array com tempos de serviço
	 * @param numClientes número de clientes no sistema
	 */
	Config(int numRecursos, double[] vi, double[] si, int numClientes) {
		this.numRecursos = numRecursos;
		this.vi = vi;
		this.si = si;
		this.numClientes = numClientes;
	}
}

/**
 * Classe para armazenar os resultados calculados pelo algoritmo MVA
 * Contém métricas individuais por dispositivo e métricas agregadas do sistema
 */
class Saida {
	double r;           // Tempo médio de resposta total do sistema
	double[] w, u, n;   // Arrays com tempo de espera, utilização e número de clientes por dispositivo
	double wSistema, uSistema, nSistema; // Métricas agregadas do sistema

	/**
	 * Construtor da classe Saida
	 * @param r tempo médio de resposta total
	 * @param w array com tempos médios de espera por dispositivo
	 * @param u array com utilização por dispositivo
	 * @param n array com número médio de clientes por dispositivo
	 * @param wSistema tempo médio de espera do sistema
	 * @param uSistema utilização total do sistema
	 * @param nSistema número médio de clientes no sistema
	 */
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

/**
 * Classe para calcular e armazenar tempos médios de resposta
 * Calcula tanto os tempos individuais (Ri) quanto o tempo total do sistema (R0)
 */
class TempoMedioResposta {
	private double[] ri;  // Tempos de resposta individuais por dispositivo
	private double r0;    // Tempo de resposta total do sistema

	TempoMedioResposta() {
	}
	
	/**
	 * Construtor que calcula o tempo de resposta total do sistema
	 * Fórmula: R0 = Σ(Vi * Ri) - soma ponderada dos tempos individuais
	 * @param ri array com tempos de resposta individuais
	 * @param vi array com taxas de visita (pesos)
	 */
	TempoMedioResposta(double[] ri, double[] vi) {
		this.ri = ri;
		for (int i = 0; i < ri.length; i++) {
			this.r0 += vi[i] * ri[i];
		}
	}

	/**
	 * @return array com tempos de resposta individuais por dispositivo
	 */
	double[] getRi() {
		return ri;
	}

	/**
	 * @return tempo de resposta total do sistema
	 */
	double getR0() {
		return r0;
	}
}
