const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'CRITICA'];
const id = document.getElementById('pagina').dataset.id;

let chamadoAtual = null;

paginaProtegida(async () => {
    const equipe = Sessao.temPerfil('ADMIN', 'TECNICO');
    const chamado = id ? await api('GET', '/chamados/' + id) : null;
    const categorias = await api('GET', '/categorias');

    preencherSelect(document.getElementById('prioridade'), PRIORIDADES, null, null,
        chamado ? chamado.prioridade : 'MEDIA');
    preencherSelect(document.getElementById('categoriaId'), categorias, 'id', 'nome',
        chamado && chamado.categoriaId, 'Selecione');

    if (equipe) {
        const [clientes, tecnicos, admins] = await Promise.all([
            api('GET', '/usuarios?perfil=CLIENTE'),
            api('GET', '/usuarios?perfil=TECNICO'),
            api('GET', '/usuarios?perfil=ADMIN')
        ]);
        const clienteSelect = document.getElementById('clienteId');
        preencherSelect(clienteSelect, clientes, 'id', 'nome', chamado && chamado.clienteId, 'Selecione');
        clienteSelect.required = true;
        preencherSelect(document.getElementById('tecnicoId'), tecnicos.concat(admins), 'id', 'nome',
            chamado && chamado.tecnicoId, 'Sem técnico');
    }

    if (chamado) {
        document.getElementById('titulo').value = chamado.titulo;
        document.getElementById('descricao').value = chamado.descricao;
        chamadoAtual = chamado;
    }

    document.getElementById('form-chamado').addEventListener('submit', salvar);
});

async function salvar(evento) {
    evento.preventDefault();
    limparMensagens();
    const dados = dadosFormulario(evento.target);
    const equipe = Sessao.temPerfil('ADMIN', 'TECNICO');

    const corpo = {
        titulo: dados.titulo,
        descricao: dados.descricao,
        prioridade: dados.prioridade,
        categoriaId: dados.categoriaId ? Number(dados.categoriaId) : null,
        clienteId: equipe && dados.clienteId ? Number(dados.clienteId) : null,
        tecnicoId: equipe && dados.tecnicoId ? Number(dados.tecnicoId) : null
    };

    const botao = document.getElementById('btn-salvar');
    botao.disabled = true;
    try {
        let salvo;
        if (id) {
            if (!equipe) {
                corpo.tecnicoId = chamadoAtual.tecnicoId;
            }
            corpo.status = chamadoAtual.status;
            salvo = await api('PUT', '/chamados/' + id, corpo);
        } else {
            salvo = await api('POST', '/chamados', corpo);
        }
        window.location.href = '/app/chamados/' + salvo.id;
    } catch (e) {
        mostrarErro(e);
        botao.disabled = false;
    }
}
