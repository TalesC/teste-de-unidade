package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {
	
	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Carro Clássico").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		//inicializa mock
		//LeilaoDao daoFalso = mock(LeilaoDao.class);
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		//define comportamento do mock
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
		encerrador.encerra();
		
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		assertEquals(2, encerrador.getTotalEncerrados());
		
	}
	
	@Test
	public void naoDeveEncerrarLeiloesDaSemanaCorrente() {
		
		Calendar antiga = Calendar.getInstance();
		antiga.set(2017, 10, 9);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Carro Clássico").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		//inicializa mock
		//LeilaoDao daoFalso = mock(LeilaoDao.class);
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		//define comportamento do mock
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
		encerrador.encerra();
		
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());
		assertEquals(0, encerrador.getTotalEncerrados());
		
		verify(daoFalso, never()).atualiza(leilao1);
		verify(daoFalso, never()).atualiza(leilao1);
		
	}
	
	@Test
	public void naoDeveEncerrarQuandoNaoHouverLeiloes() {
		
		//LeilaoDao daoFalso = mock(LeilaoDao.class);
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
		encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());		
	}
	
	//Não é possivel Mockar método estático
	/*@Test
	public void testeGenerico() {
		
		LeilaoDao daoFalso = mock(LeilaoDao.class);
		//when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		when(daoFalso.teste()).thenReturn("teste");
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
		encerrador.getTotalEncerrados();
		
		assertEquals(0, encerrador.getTotalEncerrados());		
	}
	*/
	
	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(antiga).constroi();

        RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();
        
        // verificando que o metodo atualiza foi realmente invocado!
        verify(daoFalso, times(1)).atualiza(leilao1);
	}
	
	
}
