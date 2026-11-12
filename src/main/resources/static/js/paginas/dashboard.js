const STATUS = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'FECHADO'];

paginaProtegida(async sessao => {
    document.getElementById('saudacao').textContent = 'Olá, ' + (sessao.nome || sessao.email) + '!';
    document.getElementById('subtitulo').textContent =
        sessao.perfil === 'CLIENTE' ? 'Seus chamados' : 'Todos os chamados';

    await Promise.all([carregarContadores(), carregarRecentes()]);
});

async function carregarContadores() {
    const respostas = await Promise.all(
        STATUS.map(status => api('GET', '/chamados' + queryString({ status, size: 1 })))
    );
    respostas.forEach((resposta, i) => {
        document.getElementById('total-' + STATUS[i]).textContent = resposta.page.totalElements;
    });
}

async function carregarRecentes() {
    const resposta = await api('GET', '/chamados' + queryString({ size: 5 }));
    const chamados = resposta.content;

    if (chamados.length === 0) {
        document.getElementById('vazio').hidden = false;
        document.getElementById('tabela-recentes').hidden = true;
        return;
    }

    document.getElementById('lista-recentes').innerHTML = chamados.map(c => {
        const link = '/app/chamados/' + c.id;
        return '<tr>'
            + '<td><a href="' + link + '">#' + c.id + '</a></td>'
            + '<td><a href="' + link + '">' + esc(c.titulo) + '</a></td>'
            + '<td>' + badge(c.status) + '</td>'
            + '<td>' + badge(c.prioridade) + '</td>'
            + '<td>' + esc(c.clienteNome) + '</td>'
            + '<td>' + formatarData(c.dataAbertura) + '</td>'
            + '</tr>';
    }).join('');
}
