/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.bookmarks.service;

import com.liferay.bookmarks.model.BookmarksFolder;
import com.liferay.bookmarks.model.BookmarksFolderConstants;
import com.liferay.bookmarks.util.BookmarksTestUtil;
import com.liferay.portal.kernel.test.ExecutionTestListeners;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Subscription;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.test.DeleteAfterTestRun;
import com.liferay.portal.test.Sync;
import com.liferay.portal.test.SynchronousDestinationExecutionTestListener;
import com.liferay.portal.test.listeners.MainServletExecutionTestListener;
import com.liferay.portal.test.runners.LiferayIntegrationJUnitTestRunner;
import com.liferay.portal.util.test.GroupTestUtil;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portal.util.test.TestPropsValues;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Wing Shun Chan
 * @author Roberto Díaz
 * @author Sergio González
 */
@ExecutionTestListeners(
	listeners = {
		MainServletExecutionTestListener.class,
		SynchronousDestinationExecutionTestListener.class
	})
@RunWith(LiferayIntegrationJUnitTestRunner.class)
@Sync
public class BookmarksFolderLocalServiceTest {

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetNoAssetFolders() throws Exception {
		BookmarksFolder folder = BookmarksTestUtil.addFolder(
			_group.getGroupId(), RandomTestUtil.randomString());

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(
			BookmarksFolder.class.getName(), folder.getFolderId());

		Assert.assertNotNull(assetEntry);

		AssetEntryLocalServiceUtil.deleteAssetEntry(assetEntry);

		List<BookmarksFolder> folders =
			BookmarksFolderLocalServiceUtil.getNoAssetFolders();

		Assert.assertEquals(1, folders.size());
		Assert.assertEquals(folder, folders.get(0));
	}

	@Test
	public void testMoveFolder() throws Exception {
		BookmarksFolder folder = BookmarksTestUtil.addFolder(
			_group.getGroupId(), RandomTestUtil.randomString());

		long initialParentFolderId = folder.getParentFolderId();

		BookmarksFolder destinationFolder = BookmarksTestUtil.addFolder(
			_group.getGroupId(), RandomTestUtil.randomString());

		folder = BookmarksFolderLocalServiceUtil.moveFolder(
			folder.getFolderId(), destinationFolder.getFolderId());

		Assert.assertNotEquals(
			initialParentFolderId, folder.getParentFolderId());
		Assert.assertEquals(
			destinationFolder.getFolderId(), folder.getParentFolderId());
	}

	@Test
	public void testSubscribeFolder() throws Exception {
		BookmarksFolder folder = BookmarksTestUtil.addFolder(
			_group.getGroupId(), RandomTestUtil.randomString());

		testSubscribeFolder(folder.getFolderId(), folder.getFolderId());
	}

	@Test
	public void testSubscribeRootFolder() throws Exception {
		testSubscribeFolder(
			BookmarksFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			_group.getGroupId());
	}

	@Test
	public void testUnsubscribeFolder() throws Exception {
		BookmarksFolder folder = BookmarksTestUtil.addFolder(
			_group.getGroupId(), RandomTestUtil.randomString());

		testUnsubscribeFolder(folder.getFolderId(), folder.getFolderId());
	}

	@Test
	public void testUnsubscribeRootFolder() throws Exception {
		testUnsubscribeFolder(
			BookmarksFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			_group.getGroupId());
	}

	protected void testSubscribeFolder(
			long folderId, long expectedSubscriptionClassPK)
		throws Exception {

		int initialUserSubscriptionsCount =
			SubscriptionLocalServiceUtil.getUserSubscriptionsCount(
				TestPropsValues.getUserId());

		BookmarksFolderLocalServiceUtil.subscribeFolder(
			TestPropsValues.getUserId(), _group.getGroupId(), folderId);

		Assert.assertEquals(
			initialUserSubscriptionsCount + 1,
			SubscriptionLocalServiceUtil.getUserSubscriptionsCount(
				TestPropsValues.getUserId()));

		List<Subscription> subscriptions =
			SubscriptionLocalServiceUtil.getUserSubscriptions(
				TestPropsValues.getUserId(), BookmarksFolder.class.getName());

		for (Subscription subscription : subscriptions) {
			if (subscription.getClassName().equals(
					BookmarksFolder.class.getName()) &&
				(subscription.getClassPK() == expectedSubscriptionClassPK)) {

				return;
			}
		}

		Assert.fail("Subscription does not exist");
	}

	protected void testUnsubscribeFolder(
			long folderId, long expectedSubscriptionClassPK)
		throws Exception {

		int initialUserSubscriptionsCount =
			SubscriptionLocalServiceUtil.getUserSubscriptionsCount(
				TestPropsValues.getUserId());

		BookmarksFolderLocalServiceUtil.subscribeFolder(
			TestPropsValues.getUserId(), _group.getGroupId(), folderId);

		BookmarksFolderLocalServiceUtil.unsubscribeFolder(
			TestPropsValues.getUserId(), _group.getGroupId(), folderId);

		Assert.assertEquals(
			initialUserSubscriptionsCount,
			SubscriptionLocalServiceUtil.getUserSubscriptionsCount(
				TestPropsValues.getUserId()));

		List<Subscription> subscriptions =
			SubscriptionLocalServiceUtil.getUserSubscriptions(
				TestPropsValues.getUserId(), BookmarksFolder.class.getName());

		for (Subscription subscription : subscriptions) {
			if (subscription.getClassName().equals(
					BookmarksFolder.class.getName()) &&
				(subscription.getClassPK() == expectedSubscriptionClassPK)) {

				Assert.fail("Subscription exists");
			}
		}
	}

	@DeleteAfterTestRun
	private Group _group;

}