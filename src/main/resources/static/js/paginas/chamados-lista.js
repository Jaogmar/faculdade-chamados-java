const STATUS = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'FECHADO'];
const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'CRITICA'];
const TAMANHO_PAGINA = 10;

let filtros = {};
let pagina = 0;
let totalPaginas = 0;

paginaProtegida(async () => {
    const url = new URLSearchParams(window.location.search);
    filtros = {
        status: url.get('status'),
        prioridade: url.get('prioridade'),
        clienteId: Sessao.temPerfil('ADMIN', 'TECNICO') ? url.get('clienteId') : null
    };
    pagina = Number(url.get('page')) || 0;

    preencherSelect(document.getElementById('status'), STATUS, null, null, filtros.status, 'Todos');
    preencherSelect(document.getElementById('prioridade'), PRIORIDADES, null, null, filtros.prioridade, 'Todas');
    if (Sessao.temPerfil('ADMIN', 'TECNICO')) {
        const clientes = await api('GET', '/usuarios?perfil=CLIENTE');
        preencherSelect(document.getElementById('clienteId'), clientes, 'id', 'nome', filtros.clienteId, 'Todos');
    }

    document.getElementById('form-filtros').addEventListener('submit', evento => {
        evento.preventDefault();
        filtros = dadosFormulario(evento.target);
        irParaPagina(0);
    });
    document.getElementById('btn-limpar').addEventListener('click', () => {
        document.getElementById('form-filtros').reset();
        filtros = {};
        irParaPagina(0);
    });
    document.getElementById('btn-anterior').addEventListener('click', () => irParaPagina(pagina - 1));
    document.getElementById('btn-proxima').addEventListener('click', () => irParaPagina(pagina + 1));

    await carregar();
});

async function irParaPagina(numero) {
    pagina = numero;
    history.replaceState(null, '', '/app/chamados' + queryString({ ...filtros, page: pagina || null }));
    limparMensagens();
    try {
        await carregar();
    } catch (e) {
        mostrarErro(e);
    }
}

async function carregar() {
    const resposta = await api('GET', '/chamados'
        + queryString({ ...filtros, page: pagina, size: TAMANHO_PAGINA }));
    const chamados = resposta.content;
    totalPaginas = resposta.page.totalPages;

    document.getElementById('vazio').hidden = chamados.length > 0;
    document.getElementById('tabela').hidden = chamados.length === 0;

    document.getElementById('lista-chamados').innerHTML = chamados.map(c => {
        const link = '/app/chamados/' + c.id;
        return '<tr>'
            + '<td><a href="' + link + '">#' + c.id + '</a></td>'
            + '<td><a href="' + link + '">' + esc(c.titulo) + '</a></td>'
            + '<td>' + badge(c.status) + '</td>'
            + '<td>' + badge(c.prioridade) + '</td>'
            + '<td>' + esc(c.categoriaNome) + '</td>'
            + '<td>' + esc(c.clienteNome) + '</td>'
            + '<td>' + (c.tecnicoNome ? esc(c.tecnicoNome) : '<span class="suave">-</span>') + '</td>'
            + '<td>' + formatarData(c.dataAbertura) + '</td>'
            + '</tr>';
    }).join('');

    const atual = resposta.page.number;
    document.getElementById('info-pagina').textContent =
        'Página ' + (totalPaginas === 0 ? 0 : atual + 1) + ' de ' + totalPaginas;
    document.getElementById('btn-anterior').disabled = atual <= 0;
    document.getElementById('btn-proxima').disabled = atual + 1 >= totalPaginas;
}
