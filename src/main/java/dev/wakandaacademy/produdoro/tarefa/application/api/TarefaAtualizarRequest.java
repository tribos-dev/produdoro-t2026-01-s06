package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
public class TarefaAtualizarRequest {
    @NotBlank
    @Size(message = "Campo descrição tarefa não pode estar vazio", max = 255, min = 3)
    private String descricao;
    private UUID idArea;
    private UUID idProjeto;
    private int contagemPomodoro;
}
