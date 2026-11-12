const STATUS = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'FECHADO'];
const id = document.getElementById('pagina').dataset.id;

let chamado = null;

paginaProtegida(async () => {
    if (Sessao.temPerfil('ADMIN', 'TECNICO')) {
        const [tecnicos, admins] = await Promise.all([
            api('GET', '/usuarios?perfil=TECNICO'),
            api('GET', '/usuarios?perfil=ADMIN')
        ]);
        preencherSelect(document.getElementById('novo-status'), STATUS);
        preencherSelect(document.getElementById('novo-tecnico'), tecnicos.concat(admins),
            'id', 'nome', null, 'Sem técnico');
        document.getElementById('form-atendimento').addEventListener('submit', salvarAtendimento);
    }

    document.getElementById('btn-excluir').addEventListener('click', excluir);
    document.getElementById('form-comentario').addEventListener('submit', comentar);

    await carregar();
});

async function carregar() {
    chamado = await api('GET', '/chamados/' + id);

    document.title = '#' + chamado.id + ' ' + chamado.titulo + ' | Chamados';
    document.getElementById('titulo').textContent = '#' + chamado.id + ' ' + chamado.titulo;
    document.getElementById('descricao').textContent = chamado.descricao;
    document.getElementById('status').innerHTML = badge(chamado.status);
    document.getElementById('prioridade').innerHTML = badge(chamado.prioridade);
    document.getElementById('categoria').textContent = chamado.categoriaNome;
    document.getElementById('cliente').textContent = chamado.clienteNome;
    document.getElementById('tecnico').textContent = chamado.tecnicoNome || '-';
    document.getElementById('abertura').textContent = formatarData(chamado.dataAbertura);
    document.getElementById('fechamento').textContent = formatarData(chamado.dataFechamento);

    document.getElementById('novo-status').value = chamado.status;
    document.getElementById('novo-tecnico').value = chamado.tecnicoId ? String(chamado.tecnicoId) : '';

    mostrarComentarios(chamado.comentarios);
    mostrarHistorico(chamado.historico);
}

function mostrarComentarios(comentarios) {
    document.getElementById('sem-comentarios').hidden = comentarios.length > 0;
    document.getElementById('lista-comentarios').innerHTML = comentarios.map(c =>
        '<div class="comentario">'
        + '<strong>' + esc(c.autorNome) + '</strong> ' + badge(c.autorPerfil)
        + ' <span class="suave">' + formatarData(c.dataCriacao) + '</span>'
        + '<p class="texto">' + esc(c.texto) + '</p>'
        + '</div>'
    ).join('');
}

function mostrarHistorico(historico) {
    document.getElementById('lista-historico').innerHTML = historico.map(h =>
        '<tr>'
        + '<td>' + (h.statusAnterior ? badge(h.statusAnterior) : '—') + '</td>'
        + '<td>' + badge(h.statusNovo) + '</td>'
        + '<td>' + esc(h.responsavelNome) + '</td>'
        + '<td>' + formatarData(h.dataAlteracao) + '</td>'
        + '</tr>'
    ).join('');
}

async function salvarAtendimento(evento) {
    evento.preventDefault();
    limparMensagens();
    const dados = dadosFormulario(evento.target);
    try {
        await api('PUT', '/chamados/' + id, {
            titulo: chamado.titulo,
            descricao: chamado.descricao,
            prioridade: chamado.prioridade,
            clienteId: chamado.clienteId,
            categoriaId: chamado.categoriaId,
            tecnicoId: dados.tecnicoId ? Number(dados.tecnicoId) : null,
            status: dados.status
        });
        await carregar();
        mostrarSucesso('Atendimento atualizado.');
    } catch (e) {
        mostrarErro(e);
    }
}

async function comentar(evento) {
    evento.preventDefault();
    limparMensagens();
    const form = evento.target;
    try {
        await api('POST', '/chamados/' + id + '/comentarios', { texto: dadosFormulario(form).texto });
        form.reset();
        await carregar();
    } catch (e) {
        mostrarErro(e);
    }
}

async function excluir() {
    const botao = document.getElementById('btn-excluir');
    if (botao.dataset.confirmar !== 'sim') {
        botao.dataset.confirmar = 'sim';
        botao.textContent = 'Confirmar exclusão';
        return;
    }
    botao.disabled = true;
    try {
        await api('DELETE', '/chamados/' + id);
        window.location.href = '/app/chamados';
    } catch (e) {
        mostrarErro(e);
        botao.disabled = false;
        botao.dataset.confirmar = '';
        botao.textContent = 'Excluir';
    }
}
