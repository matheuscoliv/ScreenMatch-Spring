package br.com.alura.screenmatchspring.principal;

import br.com.alura.screenmatchspring.model.DadosEpisodio;
import br.com.alura.screenmatchspring.model.DadosSerie;
import br.com.alura.screenmatchspring.model.DadosTemporada;
import br.com.alura.screenmatchspring.model.Episodio;
import br.com.alura.screenmatchspring.service.ConsumoAPI;
import br.com.alura.screenmatchspring.service.ConverteDados;

import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();


    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=92ad0654";


    public void exibeMenu() {

        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//        List<String> nomes = Arrays.asList("aaa", "bbb", "adadaada", "adadjkq");

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        //collect(.Collectors. Collect é usado quando a lista precisa ser alterada em algum momento.
        //gera uma lista que pode ser alterada posteriormente
        //.toList() gera uma lista IMUTAVEL que nao pode ser alterada posteriormente.


        System.out.println("====TOP 5 EPISODIOS====");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao() .equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodio = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodio.forEach(System.out::println);


        System.out.println("A partir de que ano voce deseja ver os episodios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);


        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodio.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " Episodio: " + e.getTitulo() +
                                " Data Lancamento " + e.getDataLancamento().format(formatador)
                ));
    }
}
