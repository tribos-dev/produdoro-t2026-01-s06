package dev.wakandaacademy.produdoro.tarefa.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                                          @PathVariable UUID idTarefa);

        @DeleteMapping("/{idUsuario}/limpar-todas-tarefas")
        @ResponseStatus(code = HttpStatus.NO_CONTENT)
        void limparTodasTarefas(@RequestHeader(name = "Authorization", required = true) String token,
                        @PathVariable UUID idUsuario);

        @PatchMapping("/{idTarefa}/ativar")
        @ResponseStatus(code = HttpStatus.NO_CONTENT)
        void ativaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                        @PathVariable UUID idTarefa);

    @GetMapping("/usuario/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaResumidoResponse> retornaTodasTarefas(@RequestHeader(name = "Authorization", required = true) String token, @PathVariable UUID idUsuario);

    @PatchMapping("/{idTarefa}/concluir")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void concluiTarefa(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idTarefa
    );
    @PatchMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void atualizaTarefa(@RequestHeader(name = "Authorization",required = true) String token,
                        @PathVariable UUID idTarefa,
                        @RequestBody @Valid TarefaAtualizarRequest tarefaAtualizarRequest);
    @PatchMapping("/{idTarefa}/incrementa-pomodoro")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void incrementaPomodoro(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idTarefa);


    @DeleteMapping("/concluidas")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTarefasConcluidas(@RequestHeader(name = "Authorization", required = true) String token);
}
