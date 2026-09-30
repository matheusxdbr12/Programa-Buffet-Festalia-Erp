package com.festalia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitGitHubManager {

    private static final String DIR_PROJETO = "c:\\Users\\matheus.souza\\Documents\\Programa do BUFFET GUI";
    private static final File ARQUIVO_LOG = new File(DIR_PROJETO, "git_github_execucao.log");

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(new FileWriter(ARQUIVO_LOG, false))) {
            log.println("=================================================");
            log.println("INICIANDO GERENCIAMENTO DO GIT E GITHUB REPOSITORIO");
            log.println("=================================================");
            System.out.println("Iniciando GitGitHubManager...");

            // 1. Localizar executáveis do Git e GH
            String gitExe = localizarExecutavel("git", Arrays.asList(
                    "git",
                    "C:\\Program Files\\Git\\cmd\\git.exe",
                    "C:\\Program Files\\Git\\bin\\git.exe",
                    "C:\\Users\\matheus.souza\\AppData\\Local\\Programs\\Git\\cmd\\git.exe",
                    "C:\\Users\\matheus.souza\\AppData\\Local\\Programs\\Git\\bin\\git.exe",
                    "C:\\ProgramData\\chocolatey\\bin\\git.exe"
            ), log);

            String ghExe = localizarExecutavel("gh", Arrays.asList(
                    "gh",
                    "C:\\Program Files\\GitHub CLI\\gh.exe",
                    "C:\\Users\\matheus.souza\\AppData\\Local\\Programs\\GitHub CLI\\gh.exe",
                    "C:\\ProgramData\\chocolatey\\bin\\gh.exe",
                    "C:\\Users\\matheus.souza\\scoop\\shims\\gh.exe"
            ), log);

            log.println("Executável Git selecionado: " + gitExe);
            log.println("Executável GitHub CLI (GH) selecionado: " + ghExe);

            // 2. Verificar status da autenticação do GitHub CLI
            log.println("\n--- [1] Verificando autenticação no GitHub ---");
            executarComando(log, ghExe, "auth", "status");

            // 3. Obter nome do usuário do GitHub
            log.println("\n--- [2] Obtendo dados da conta GitHub ---");
            String userJson = executarComando(log, ghExe, "api", "user");
            log.println("Resposta API User: " + (userJson.length() > 200 ? userJson.substring(0, 200) + "..." : userJson));

            // 4. Inicializar / Configurar Git local
            log.println("\n--- [3] Configuração do Git local ---");
            executarComando(log, gitExe, "init");
            executarComando(log, gitExe, "branch", "-M", "main");

            // Configurar user.name e user.email caso ainda não estejam definidos
            String gitUser = executarComandoSilencioso(gitExe, "config", "user.name");
            if (gitUser == null || gitUser.trim().isEmpty()) {
                executarComando(log, gitExe, "config", "user.name", "Matheus Souza");
                executarComando(log, gitExe, "config", "user.email", "matheus.souza@festalia.com");
            }

            // 5. Verificar repositórios existentes do usuário
            log.println("\n--- [4] Listando repositórios do usuário no GitHub ---");
            executarComando(log, ghExe, "repo", "list", "--limit", "10");

            // 6. Preparar commit com as novas funcionalidades
            log.println("\n--- [5] Preparando arquivos e commit das funcionalidades ---");
            executarComando(log, gitExe, "status");
            executarComando(log, gitExe, "add", ".");

            String mensagemCommit = "feat: Sistema de OS Festalia v2.0 com marca d'agua dinamica, logo customizavel do buffet e calculo de taxas";
            executarComando(log, gitExe, "commit", "-m", mensagemCommit);

            // 7. Criar repositório no GitHub se ainda não existir
            log.println("\n--- [6] Criando e sincronizando repositório remoto no GitHub ---");
            String nomeRepo = "programa-buffet-festalia-erp";
            String descricao = "Sistema ERP Desktop em Java para Gestão de Buffets, Orçamentos, Cardápios e Emissão de Ordens de Serviço em PDF com Marca D'Água customizável.";

            // Tenta criar o repositório remoto com o gh cli
            String resultadoCreate = executarComando(log, ghExe, "repo", "create", nomeRepo,
                    "--public",
                    "--source=.",
                    "--remote=origin",
                    "--description=" + descricao,
                    "--push");

            // Se o repositório já existia ou remote origin já existia
            if (resultadoCreate.contains("already exists") || resultadoCreate.contains("remote origin already exists")) {
                log.println("Repositório remoto já existe ou remote já configurado. Realizando push...");
                executarComando(log, gitExe, "push", "-u", "origin", "main");
            }

            // 8. Status final do repositório
            log.println("\n--- [7] Verificação Final ---");
            executarComando(log, gitExe, "remote", "-v");
            executarComando(log, gitExe, "log", "-2", "--oneline");
            executarComando(log, ghExe, "repo", "view");

            log.println("\n=================================================");
            log.println("PROCESSO CONCLUÍDO COM SUCESSO!");
            log.println("=================================================");
            System.out.println("Processo concluído! Verifique o log em: " + ARQUIVO_LOG.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String localizarExecutavel(String nomeBase, List<String> candidatos, PrintWriter log) {
        for (String c : candidatos) {
            try {
                ProcessBuilder pb = new ProcessBuilder(c, "--version");
                pb.directory(new File(DIR_PROJETO));
                Process p = pb.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    log.println("Encontrado '" + nomeBase + "' em: " + c);
                    return c;
                }
            } catch (Exception ignored) {}
        }
        return nomeBase; // Fallback
    }

    private static String executarComando(PrintWriter log, String... comando) {
        StringBuilder output = new StringBuilder();
        try {
            log.println(">>> Executando: " + String.join(" ", comando));
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.directory(new File(DIR_PROJETO));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    log.println("    " + linha);
                    output.append(linha).append("\n");
                }
            }
            int code = p.waitFor();
            log.println("    [Exit Code: " + code + "]");
        } catch (Exception e) {
            log.println("    [Erro na execução: " + e.getMessage() + "]");
        }
        return output.toString();
    }

    private static String executarComandoSilencioso(String... comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.directory(new File(DIR_PROJETO));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String l;
                while ((l = reader.readLine()) != null) {
                    sb.append(l).append("\n");
                }
            }
            p.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
