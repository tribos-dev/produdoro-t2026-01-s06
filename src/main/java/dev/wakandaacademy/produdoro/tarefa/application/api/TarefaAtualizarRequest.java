package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TarefaAtualizarRequest {
    @NotBlank
    @Size(message = "Campo descrição tarefa não pode estar vazio", max = 255, min = 3)
    private String descricao;
}
