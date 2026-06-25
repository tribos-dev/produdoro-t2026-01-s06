package dev.wakandaacademy.produdoro.usuario.application.service;

import javax.validation.Valid;

import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import dev.wakandaacademy.produdoro.credencial.application.service.CredencialService;
import dev.wakandaacademy.produdoro.pomodoro.application.service.PomodoroService;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioCriadoResponse;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UsuarioApplicationService implements UsuarioService {
    private final PomodoroService pomodoroService;
    private final CredencialService credencialService;
    private final UsuarioRepository usuarioRepository;

    @Override
    public UsuarioCriadoResponse criaNovoUsuario(@Valid UsuarioNovoRequest usuarioNovo) {
        log.info("[inicia] UsuarioApplicationService - criaNovoUsuario");
        var configuracaoPadrao = pomodoroService.getConfiguracaoPadrao();
        credencialService.criaNovaCredencial(usuarioNovo);
        var usuario = new Usuario(usuarioNovo, configuracaoPadrao);
        usuarioRepository.salva(usuario);
        log.info("[finaliza] UsuarioApplicationService - criaNovoUsuario");
        return new UsuarioCriadoResponse(usuario);
    }

    @Override
    public UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario) {
        log.info("[inicia] UsuarioApplicationService - buscaUsuarioPorId");
        Usuario usuario = usuarioRepository.buscaUsuarioPorId(idUsuario);
        log.info("[finaliza] UsuarioApplicationService - buscaUsuarioPorId");
        return new UsuarioCriadoResponse(usuario);
    }

    @Override
    public void iniciarPausaLonga(UUID idUsuario, String usuario) {
        log.info("[inicia] UsuarioApplicationService - iniciarPausaLonga");
        Usuario usuarioPausa = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPausa.validaIdUsuario(idUsuario);
        usuarioPausa.alterarStatusParaPausaLonga();
        usuarioRepository.salva(usuarioPausa);
        log.info("[finaliza] UsuarioApplicationService - iniciarPausaLonga");
    }

    @Override
    public void iniciaFoco(String usuarioEmail, UUID idUsuario) {
        log.info("[inicia] UsuarioApplicationService - iniciaFoco");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
        usuario.validaIdUsuario(idUsuario);
        usuario.iniciaFoco();
        usuarioRepository.salva(usuario);
        log.info("[finaliza] UsuarioApplicationService - iniciaFoco");
    }

}
