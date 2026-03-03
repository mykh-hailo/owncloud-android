/**
 * ownCloud Android client application
 *
 * Copyright (C) 2025 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.presentation.files.filelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.owncloud.android.R
import com.owncloud.android.databinding.SpaceHeaderBinding
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PATH
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.presentation.thumbnails.ThumbnailsRequester
import coil.load

/**
 * Adapter that shows a single space header item as the first item in a file list.
 * Used with [androidx.recyclerview.widget.ConcatAdapter] so the header scrolls with the list.
 */
class SpaceHeaderAdapter : RecyclerView.Adapter<SpaceHeaderAdapter.SpaceHeaderViewHolder>() {

    data class State(
        val space: OCSpace?,
        val folderRemotePath: String?,
        val fileListOption: FileListOption,
        val isMultiPersonal: Boolean,
    ) {
        val showHeader: Boolean
            get() = (space?.isProject == true || (space?.isPersonal == true && isMultiPersonal)) &&
                folderRemotePath == ROOT_PATH &&
                fileListOption != FileListOption.AV_OFFLINE
    }

    var state: State = State(null, null, FileListOption.ALL_FILES, false)
        set(value) {
            val hadHeader = field.showHeader
            val hasHeader = value.showHeader
            val contentChanged = field.space != value.space
            field = value
            when {
                hadHeader && !hasHeader -> notifyItemRemoved(0)
                !hadHeader && hasHeader -> notifyItemInserted(0)
                hasHeader && contentChanged -> notifyItemChanged(0)
            }
        }

    override fun getItemCount(): Int = if (state.showHeader) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpaceHeaderViewHolder {
        val binding = SpaceHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpaceHeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpaceHeaderViewHolder, position: Int) {
        holder.bind(state.space)
    }

    override fun onViewAttachedToWindow(holder: SpaceHeaderViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true
    }

    class SpaceHeaderViewHolder(
        private val binding: SpaceHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(space: OCSpace?) {
            binding.spaceHeaderName.text = space?.name
            binding.spaceHeaderSubtitle.text = space?.description
            val spaceSpecialImage = space?.getSpaceSpecialImage()
            if (spaceSpecialImage != null) {
                binding.spaceHeaderImage.load(
                    ThumbnailsRequester.getPreviewUriForSpaceSpecial(spaceSpecialImage),
                    ThumbnailsRequester.getCoilImageLoader()
                ) {
                    placeholder(R.drawable.ic_spaces)
                    error(R.drawable.ic_spaces)
                }
            } else {
                binding.spaceHeaderImage.setImageResource(R.drawable.ic_spaces)
            }
        }
    }
}
