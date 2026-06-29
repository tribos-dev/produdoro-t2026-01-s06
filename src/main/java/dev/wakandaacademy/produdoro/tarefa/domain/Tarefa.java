package dev.wakandaacademy.produdoro.tarefa.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Document(collection = "Tarefa")
public class Tarefa {
	@Id
	private UUID idTarefa;
	@NotBlank
	private String descricao;
	@Indexed
	private UUID idUsuario;
	@Indexed
	private UUID idArea;
	@Indexed
	private UUID idProjeto;
	private StatusTarefa status;
	private StatusAtivacaoTarefa statusAtivacao;
	private int contagemPomodoro;
	private int posicao;

	public Tarefa(TarefaRequest tarefaRequest, int novaPosicao) {
		this.idTarefa = UUID.randomUUID();
		this.idUsuario = tarefaRequest.getIdUsuario();
		this.descricao = tarefaRequest.getDescricao();
		this.idArea = tarefaRequest.getIdArea();
		this.idProjeto = tarefaRequest.getIdProjeto();
		this.status = StatusTarefa.A_FAZER;
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
		this.contagemPomodoro = 1;
		this.posicao = novaPosicao;
	}

	public void pertenceAoUsuario(Usuario usuarioPorEmail) {
		if(!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da Tarefa solicitada!");
		}
	}

	public void concluir() {
		if (StatusTarefa.CONCLUIDA.equals(this.status)) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Status da  tarefa já é concluido!");
		}
		this.status = StatusTarefa.CONCLUIDA;
	}

	public void validaNaoEstaAtiva() {
		if (this.statusAtivacao == StatusAtivacaoTarefa.ATIVA) {
			throw APIException.build(HttpStatus.CONFLICT, "Tarefa já está ativa!");
		}
	}

	public void ativa() {
		this.statusAtivacao = StatusAtivacaoTarefa.ATIVA;
	}

	public void desativa() {
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
	}

	public void incrementaPomodoro() {
		this.contagemPomodoro++;
	}

    public void atualizaTarefa(String novaDescricao) {
        validaDescricaoVazia(novaDescricao);
        this.descricao = novaDescricao;
    }

    private static void validaDescricaoVazia(String novaDescricao) {
        if(novaDescricao.isBlank())
            throw APIException.build(HttpStatus.BAD_REQUEST, "O campo não pode estar vazio");
    }

	public void modificaPosicao(int novaPosicao){
		this.posicao = novaPosicao;
	}
}
