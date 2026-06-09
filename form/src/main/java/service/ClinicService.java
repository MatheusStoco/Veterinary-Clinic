package service;

import database.dao.*;
import database.exception.BusinessException;
import database.model.*;
import gui.model.Calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * RF-01: Facade — ponto de entrada único da camada de negócio.
 * Expõe métodos de alto nível ao MainController, sem expor
 * a complexidade interna dos DAOs e das regras de validação.
 */
public class ClinicService {

    private final AnimalDao animalDao;
    private final MedicDao medicDao;
    private final SurgeryDao surgeryDao;
    private final ClientDao clientDao;
    private final AppointmentDao appointmentDao;
    private final Calendar calendar;

    public ClinicService() {
        this.animalDao      = new AnimalDao();
        this.medicDao       = new MedicDao();
        this.surgeryDao     = new SurgeryDao();
        this.clientDao      = new ClientDao();
        this.appointmentDao = new AppointmentDao();
        this.calendar       = new Calendar();
    }

    // ── Busca de dados ─────────────────────────────────────────

    public List<String> buscarEspecies() {
        return animalDao.getAllSpecies();
    }

    public List<AnimalEntity> buscarAnimais() {
        return animalDao.getAll();
    }

    public List<AnimalEntity> buscarAnimaisPorEspecie(String especie) {
        return animalDao.getAllBySpecies(especie);
    }

    public List<MedicEntity> buscarMedicos() {
        return medicDao.getAll();
    }

    public List<SurgeryEntity> buscarProcedimentos() {
        return surgeryDao.getAll();
    }

    // ── Disponibilidade de horários (RF-04: vem de Calendar) ───

    public void configurarAgenda(MedicEntity medico, SurgeryEntity procedimento) {
        calendar.setMedic(medico);
        calendar.setSurgery(procedimento);
    }

    public boolean verificarDisponibilidade(LocalDate data) {
        return calendar.isDayAvailable(data);
    }

    public List<LocalTime> buscarHorariosLivres(LocalDate data) {
        return calendar.getFreeHours(data);
    }

    // ── Agendamento ────────────────────────────────────────────

    /**
     * RF-01: lógica de negócio extraída do MainController.
     * Cria o cliente e a consulta em sequência.
     * RF-06: lança BusinessException para violações de regra.
     */
    public void agendarConsulta(
            String nome, String sobrenome, String cpf,
            String telefone, String email,
            AnimalEntity animal, MedicEntity medico,
            SurgeryEntity procedimento, LocalDate data,
            LocalTime hora, String observacoes) {

        validarCampos(nome, sobrenome, cpf, telefone, email, animal, medico, procedimento, data, hora);

        ClientEntity cliente = new ClientEntity();
        cliente.setFirstName(nome);
        cliente.setLastName(sobrenome);
        cliente.setTin(cpf);
        cliente.setPhoneNumber(telefone);
        cliente.setEmail(email);
        clientDao.create(cliente);

        AppointmentEntity consulta = new AppointmentEntity();
        consulta.setDate(data);
        consulta.setHour(hora);
        consulta.setNotes(observacoes);
        consulta.setSurgery(procedimento);
        consulta.setMedic(medico);
        consulta.setClient(cliente);
        consulta.setAnimal(animal);
        appointmentDao.create(consulta);
    }

    private void validarCampos(
            String nome, String sobrenome, String cpf,
            String telefone, String email,
            AnimalEntity animal, MedicEntity medico,
            SurgeryEntity procedimento, LocalDate data, LocalTime hora) {

        if (nome == null || nome.isBlank())
            throw new BusinessException("Nome é obrigatório.");
        if (sobrenome == null || sobrenome.isBlank())
            throw new BusinessException("Sobrenome é obrigatório.");
        if (cpf == null || cpf.isBlank())
            throw new BusinessException("CPF é obrigatório.");
        if (telefone == null || telefone.isBlank())
            throw new BusinessException("Telefone é obrigatório.");
        if (email == null || email.isBlank())
            throw new BusinessException("E-mail é obrigatório.");
        if (animal == null)
            throw new BusinessException("Animal é obrigatório.");
        if (medico == null)
            throw new BusinessException("Médico é obrigatório.");
        if (procedimento == null)
            throw new BusinessException("Procedimento é obrigatório.");
        if (data == null)
            throw new BusinessException("Data é obrigatória.");
        if (hora == null)
            throw new BusinessException("Horário é obrigatório.");
    }
}
