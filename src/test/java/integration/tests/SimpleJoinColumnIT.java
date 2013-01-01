package integration.tests;

import static fr.doan.achilles.columnFamily.ColumnFamilyHelper.normalizeCanonicalName;
import static fr.doan.achilles.common.CassandraDaoTest.getCluster;
import static fr.doan.achilles.common.CassandraDaoTest.getEntityDao;
import static fr.doan.achilles.common.CassandraDaoTest.getKeyspace;
import static fr.doan.achilles.entity.metadata.PropertyType.JOIN_SIMPLE;
import static org.fest.assertions.api.Assertions.assertThat;
import integration.tests.entity.Tweet;
import integration.tests.entity.TweetTestBuilder;
import integration.tests.entity.User;
import integration.tests.entity.UserTestBuilder;
import java.util.List;
import java.util.UUID;
import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.DynamicComposite;
import org.apache.cassandra.utils.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import fr.doan.achilles.dao.GenericEntityDao;
import fr.doan.achilles.entity.factory.ThriftEntityManagerFactoryImpl;
import fr.doan.achilles.entity.manager.ThriftEntityManager;
import fr.doan.achilles.serializer.Utils;

/**
 * ThriftEntityManagerDirtyCheckIT
 * 
 * @author DuyHai DOAN
 * 
 */
public class SimpleJoinColumnIT {
    private final String ENTITY_PACKAGE = "integration.tests.entity";

    private GenericEntityDao<UUID> tweetDao = getEntityDao(Utils.UUID_SRZ,
            normalizeCanonicalName(Tweet.class.getCanonicalName()));

    private GenericEntityDao<Long> userDao = getEntityDao(Utils.LONG_SRZ,
            normalizeCanonicalName(User.class.getCanonicalName()));

    private ThriftEntityManagerFactoryImpl factory = new ThriftEntityManagerFactoryImpl(getCluster(), getKeyspace(),
            ENTITY_PACKAGE, true);

    private ThriftEntityManager em = (ThriftEntityManager) factory.createEntityManager();

    private Tweet tweet;
    private User creator;
    private Long creatorId = 3849L;

    @Before
    public void setUp() {
        creator = UserTestBuilder.user().id(creatorId).firstname("fn").lastname("ln").buid();
    }

    @Test
    public void should_persist_user_and_then_tweet() throws Exception {

        em.persist(creator);

        tweet = TweetTestBuilder.tweet().randomId().content("this is a tweet").creator(creator).buid();

        em.persist(tweet);

        DynamicComposite startComp = new DynamicComposite();
        startComp.addComponent(0, JOIN_SIMPLE.flag(), ComponentEquality.EQUAL);

        DynamicComposite endComp = new DynamicComposite();
        endComp.addComponent(0, JOIN_SIMPLE.flag(), ComponentEquality.GREATER_THAN_EQUAL);

        List<Pair<DynamicComposite, Object>> columns = tweetDao.findColumnsRange(tweet.getId(), startComp, endComp,
                false, 20);

        assertThat(columns).hasSize(1);

        Pair<DynamicComposite, Object> creator = columns.get(0);
        assertThat(creator.right).isEqualTo(creatorId);

    }

    @Test
    public void should_find_user_from_tweet_after_persist() throws Exception {

        em.persist(creator);

        tweet = TweetTestBuilder.tweet().randomId().content("this is a tweet").creator(creator).buid();

        em.persist(tweet);

        tweet = em.find(Tweet.class, tweet.getId());

        User joinUser = tweet.getCreator();

        assertThat(joinUser).isNotNull();
        assertThat(joinUser.getId()).isEqualTo(creatorId);
        assertThat(joinUser.getFirstname()).isEqualTo("fn");
        assertThat(joinUser.getLastname()).isEqualTo("ln");

    }

    @Test
    public void should_find_user_from_tweet_after_merge() throws Exception {

        em.persist(creator);

        tweet = TweetTestBuilder.tweet().randomId().content("this is a tweet").creator(creator).buid();

        tweet = em.merge(tweet);

        User joinUser = tweet.getCreator();

        assertThat(joinUser).isNotNull();
        assertThat(joinUser.getId()).isEqualTo(creatorId);
        assertThat(joinUser.getFirstname()).isEqualTo("fn");
        assertThat(joinUser.getLastname()).isEqualTo("ln");

    }

    @Test
    public void should_find_user_from_tweet_after_refresh() throws Exception {

        em.persist(creator);

        tweet = TweetTestBuilder.tweet().randomId().content("this is a tweet").creator(creator).buid();

        tweet = em.merge(tweet);

        User joinUser = tweet.getCreator();

        em.refresh(tweet);

        joinUser = tweet.getCreator();

        assertThat(joinUser).isNotNull();
        assertThat(joinUser.getId()).isEqualTo(creatorId);
        assertThat(joinUser.getFirstname()).isEqualTo("fn");
        assertThat(joinUser.getLastname()).isEqualTo("ln");

    }

    @After
    public void tearDown() {
        tweetDao.truncate();
    }
}