const TAMANHO_PAGINA = 10;
const id = document.getElementById('pagina').dataset.id;

let pagina = 0;

paginaProtegida(async sessao => {
    document.getElementById('btn-editar').hidden =
        !(Sessao.temPerfil('ADMIN') || String(sessao.id) === id);
    document.getElementById('btn-excluir').addEventListener('click', excluir);

    const usuario = await api('GET', '/usuarios/' + id);
    document.title = usuario.nome + ' | Chamados';
    document.getElementById('titulo').textContent = '#' + usuario.id + ' ' + usuario.nome;
    document.getElementById('nome').textContent = usuario.nome;
    document.getElementById('email').textContent = usuario.email;
    document.getElementById('telefone').textContent = usuario.telefone || '-';
    document.getElementById('perfil').innerHTML = badge(usuario.perfil);
    document.getElementById('ativo').textContent = usuario.ativo ? 'Sim' : 'Não';

    if (usuario.perfil === 'CLIENTE') {
        document.getElementById('cartao-chamados').hidden = false;
        document.getElementById('btn-anterior').addEventListener('click', () => irParaPagina(pagina - 1));
        document.getElementById('btn-proxima').addEventListener('click', () => irParaPagina(pagina + 1));
        await carregarChamados();
    }
});

async function irParaPagina(numero) {
    pagina = numero;
    limparMensagens();
    try {
        await carregarChamados();
    } catch (e) {
        mostrarErro(e);
    }
}

async function carregarChamados() {
    const resposta = await api('GET', '/usuarios/' + id + '/chamados'
        + queryString({ page: pagina, size: TAMANHO_PAGINA }));
    const chamados = resposta.content;
    const totalPaginas = resposta.page.totalPages;

    document.getElementById('vazio').hidden = chamados.length > 0;
    document.getElementById('tabela').hidden = chamados.length === 0;

    document.getElementById('lista-chamados').innerHTML = chamados.map(c => {
        const link = '/app/chamados/' + c.id;
        return '<tr>'
            + '<td><a href="' + link + '">#' + c.id + '</a></td>'
            + '<td><a href="' + link + '">' + esc(c.titulo) + '</a></td>'
            + '<td>' + badge(c.status) + '</td>'
            + '<td>' + badge(c.prioridade) + '</td>'
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

async function excluir() {
    const botao = document.getElementById('btn-excluir');
    if (botao.dataset.confirmar !== 'sim') {
        botao.dataset.confirmar = 'sim';
        botao.textContent = 'Confirmar exclusão';
        return;
    }
    botao.disabled = true;
    try {
        await api('DELETE', '/usuarios/' + id);
        window.location.href = '/app/usuarios';
    } catch (e) {
        mostrarErro(e);
        botao.disabled = false;
        botao.dataset.confirmar = '';
        botao.textContent = 'Excluir';
    }
}
