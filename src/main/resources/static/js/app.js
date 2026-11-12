const Sessao = {
    CHAVE: 'chamados.sessao',

    get() {
        try {
            return JSON.parse(localStorage.getItem(this.CHAVE));
        } catch (e) {
            return null;
        }
    },

    async salvar(tokenResponse) {
        localStorage.setItem(this.CHAVE, JSON.stringify({
            token: tokenResponse.token,
            expiraEm: tokenResponse.expiraEm,
            perfil: tokenResponse.perfil
        }));
        const eu = await api('GET', '/auth/me');
        const sessao = { ...this.get(), id: eu.id, nome: eu.nome, email: eu.email };
        localStorage.setItem(this.CHAVE, JSON.stringify(sessao));
        return sessao;
    },

    limpar() {
        localStorage.removeItem(this.CHAVE);
    },

    valida() {
        const s = this.get();
        return !!(s && s.token && new Date(s.expiraEm) > new Date());
    },

    exigir() {
        if (!this.valida()) {
            this.limpar();
            window.location.href = '/app/login';
            return null;
        }
        return this.get();
    },

    temPerfil(...perfis) {
        const s = this.get();
        return !!s && perfis.includes(s.perfil);
    },

    sair() {
        this.limpar();
        window.location.href = '/app/login';
    }
};

async function api(metodo, url, corpo) {
    const headers = { 'Accept': 'application/json' };
    const sessao = Sessao.get();
    if (sessao && sessao.token) {
        headers['Authorization'] = 'Bearer ' + sessao.token;
    }
    if (corpo !== undefined) {
        headers['Content-Type'] = 'application/json';
    }

    const resposta = await fetch(url, {
        method: metodo,
        headers,
        body: corpo !== undefined ? JSON.stringify(corpo) : undefined
    });

    if (resposta.status === 401 && !url.startsWith('/auth/')) {
        Sessao.limpar();
        window.location.href = '/app/login';
        throw { status: 401, erro: 'Sessão expirada', detalhes: [] };
    }

    const texto = await resposta.text();
    const dados = texto ? JSON.parse(texto) : null;
    if (!resposta.ok) {
        throw dados || { status: resposta.status, erro: 'Erro ' + resposta.status, detalhes: [] };
    }
    return dados;
}

function queryString(params) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined && v !== '') qs.append(k, v);
    });
    const s = qs.toString();
    return s ? '?' + s : '';
}

function mostrarMensagem(tipo, titulo, detalhes) {
    const div = document.getElementById('mensagens');
    if (!div) return;
    div.innerHTML = '';
    const alerta = document.createElement('div');
    alerta.className = 'alerta alerta-' + tipo;
    const strong = document.createElement('strong');
    strong.textContent = titulo;
    alerta.appendChild(strong);
    if (detalhes && detalhes.length) {
        const ul = document.createElement('ul');
        detalhes.forEach(d => {
            const li = document.createElement('li');
            li.textContent = d;
            ul.appendChild(li);
        });
        alerta.appendChild(ul);
    }
    div.appendChild(alerta);
    window.scrollTo(0, 0);
}

function mostrarErro(e) {
    console.error(e);
    mostrarMensagem('erro', (e && (e.erro || e.message)) || 'Erro inesperado', e && e.detalhes);
}

function mostrarSucesso(msg) {
    mostrarMensagem('sucesso', msg);
}

function limparMensagens() {
    const div = document.getElementById('mensagens');
    if (div) div.innerHTML = '';
}

function formatarData(iso) {
    if (!iso) return '-';
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

const ROTULOS = {
    ABERTO: 'Aberto', EM_ATENDIMENTO: 'Em atendimento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente', FECHADO: 'Fechado',
    BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', CRITICA: 'Crítica',
    CLIENTE: 'Cliente', TECNICO: 'Técnico', ADMIN: 'Administrador'
};

function rotulo(valor) {
    return ROTULOS[valor] || valor || '-';
}

function badge(valor) {
    if (!valor) return '-';
    return '<span class="badge badge-' + valor.toLowerCase() + '">' + rotulo(valor) + '</span>';
}

function esc(texto) {
    const div = document.createElement('div');
    div.textContent = texto == null ? '' : String(texto);
    return div.innerHTML;
}

function preencherSelect(select, lista, campoValor, campoTexto, selecionado, opcaoVazia) {
    select.innerHTML = '';
    if (opcaoVazia !== undefined) {
        select.appendChild(new Option(opcaoVazia, ''));
    }
    lista.forEach(item => {
        const valor = typeof item === 'object' ? item[campoValor] : item;
        const texto = typeof item === 'object' ? item[campoTexto] : rotulo(item);
        select.appendChild(new Option(texto, valor));
    });
    if (selecionado !== undefined && selecionado !== null) {
        select.value = String(selecionado);
    }
}

function dadosFormulario(form) {
    const dados = {};
    new FormData(form).forEach((v, k) => { dados[k] = v === '' ? null : v; });
    return dados;
}

function aplicarPerfis() {
    const s = Sessao.get();
    document.querySelectorAll('[data-perfil]').forEach(el => {
        const permitidos = el.dataset.perfil.split(',').map(p => p.trim());
        el.hidden = !s || !permitidos.includes(s.perfil);
    });
    const span = document.getElementById('usuario-logado');
    if (span && s) span.textContent = (s.nome || s.email || '') + ' (' + rotulo(s.perfil) + ')';
}

function marcarMenuAtivo() {
    const caminho = window.location.pathname.replace(/\/+$/, '');
    document.querySelectorAll('.navbar a:not(.marca)').forEach(link => {
        const destino = link.getAttribute('href');
        const ativo = destino === '/app'
            ? caminho === '/app'
            : caminho === destino || caminho.startsWith(destino + '/');
        link.classList.toggle('ativo', ativo);
    });
}

function paginaProtegida(iniciar) {
    document.addEventListener('DOMContentLoaded', async () => {
        const sessao = Sessao.exigir();
        if (!sessao) return;
        aplicarPerfis();
        marcarMenuAtivo();
        try {
            await iniciar(sessao);
        } catch (e) {
            mostrarErro(e);
        }
    });
}
