package org.springframework.security.acls.mongodb;

import org.springframework.security.acls.dao.AclRepository;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.DomainObjectPermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.MongoAcl;
import org.springframework.security.acls.domain.MongoSid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.ChildrenExistException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

public class MongoDBMutableAclService extends MongoDBAclService implements MutableAclService {

	private final AclCache aclCache;

	public MongoDBMutableAclService(AclRepository repository, LookupStrategy lookupStrategy, AclCache aclCache) {
		super(repository, lookupStrategy);
		this.aclCache = aclCache;
	}

	@Override
	public MutableAcl createAcl(ObjectIdentity objectIdentity) throws AlreadyExistsException {
		Assert.notNull(objectIdentity, "Object Identity required");

		List<MongoAcl> availableAcl =
				aclRepository.findByInstanceIdAndClassName(objectIdentity.getIdentifier(), objectIdentity.getType());

		if (null != availableAcl && !availableAcl.isEmpty()) {
			throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
		}

		// Need to retrieve the current principal, in order to know who "owns" this ACL
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		PrincipalSid sid = new PrincipalSid(auth);

		MongoAcl mongoAcl = new MongoAcl(objectIdentity.getIdentifier(),
				objectIdentity.getType(),
				UUID.randomUUID().toString(),
				new MongoSid(sid.getPrincipal()),
				null,
				true);

		aclRepository.save(mongoAcl);

		// Retrieve the ACL via superclass (ensures cache registration, proper retrieval etc)
		Acl acl = readAclById(objectIdentity);
		Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be returned");

		return (MutableAcl) acl;
	}

	@Override
	public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren) throws ChildrenExistException {
		Assert.notNull(objectIdentity, "Object Identity required");
		Assert.notNull(objectIdentity.getIdentifier(),
				"Object Identity doesn't provide an identifier");

		List<ObjectIdentity> children = findChildren(objectIdentity);
		if (deleteChildren) {
			if (children != null) {
				for (ObjectIdentity child : children) {
					deleteAcl(child, true);
				}
			}
		} else if (!children.isEmpty()) {
			throw new ChildrenExistException("Cannot delete '" + objectIdentity + "' (has " + children.size() + " children)");
		}

		Long numRemoved = aclRepository.deleteByInstanceId(objectIdentity.getIdentifier());
		if (null == numRemoved || numRemoved < 1) {
			// TODO: log warning that no ACL was found for the domain object
		}

		// Clear the cache
		aclCache.evictFromCache(objectIdentity);
	}

	@Override
	public MutableAcl updateAcl(MutableAcl acl) throws NotFoundException {

		MongoAcl mongoAcl = aclRepository.findById(acl.getId().toString())
				.orElseThrow(() -> new NotFoundException("No entry for ACL " + acl.getId() + " found"));

		// Delete this ACL's ACEs in the acl_entry table
		mongoAcl.getPermissions().clear();

		for (AccessControlEntry _ace : acl.getEntries()) {
			AccessControlEntryImpl ace = (AccessControlEntryImpl) _ace;
			MongoSid sid = null;
			String aceId = (String) ace.getId();
			if (null == aceId) {
				aceId = UUID.randomUUID().toString();
			}
			if (ace.getSid() instanceof PrincipalSid) {
				PrincipalSid principal = (PrincipalSid) ace.getSid();
				sid = new MongoSid(principal.getPrincipal(), true);
			} else if (ace.getSid() instanceof GrantedAuthoritySid) {
				GrantedAuthoritySid grantedAuthority = (GrantedAuthoritySid) ace.getSid();
				sid = new MongoSid(grantedAuthority.getGrantedAuthority(), false);
			}
			DomainObjectPermission permission =
					new DomainObjectPermission(aceId, sid, ace.getPermission().getMask(),
							ace.isGranting(), ace.isAuditSuccess(), ace.isAuditFailure());
			mongoAcl.getPermissions().add(permission);
		}

		// Update the acl entry
		aclRepository.save(mongoAcl);

		// Clear the cache, including children
		clearCacheIncludingChildren(acl.getObjectIdentity());

		// Retrieve the ACL via superclass (ensures cache registration, proper retrieval etc)
		return (MutableAcl) readAclById(acl.getObjectIdentity());
	}

	private void clearCacheIncludingChildren(ObjectIdentity objectIdentity) {
		Assert.notNull(objectIdentity, "ObjectIdentity required");
		List<ObjectIdentity> children = findChildren(objectIdentity);
		if (children != null) {
			for (ObjectIdentity child : children) {
				clearCacheIncludingChildren(child);
			}
		}
		aclCache.evictFromCache(objectIdentity);
	}
}