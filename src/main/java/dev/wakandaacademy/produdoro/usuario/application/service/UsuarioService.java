package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioCriadoResponse;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;

import java.util.UUID;

public interface UsuarioService {
    UsuarioCriadoResponse criaNovoUsuario(UsuarioNovoRequest usuarioNovo);

    UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario);

    void iniciarPausaLonga(UUID idUsuario, String usuario);

    void iniciaFoco(String usuarioEmail, UUID idUsuario);
}
